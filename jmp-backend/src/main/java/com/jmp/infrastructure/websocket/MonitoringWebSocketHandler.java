package com.jmp.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmp.domain.conference.entity.Conference;
import com.jmp.application.dto.ConferenceStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time conference monitoring.
 * Implements session management and event broadcasting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringWebSocketHandler extends TextWebSocketHandler {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // Active sessions by tenant ID
    private final Map<String, ConcurrentHashMap<String, WebSocketSession>> tenantSessions = new ConcurrentHashMap<>();
    
    // Session metadata
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String tenantId = extractTenantId(session);
        String sessionId = session.getId();
        
        log.info("WebSocket connection established: {} for tenant: {}", sessionId, tenantId);
        
        tenantSessions
            .computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>())
            .put(sessionId, session);
        
        sessionMetadata.put(sessionId, new SessionMetadata(tenantId, Instant.now()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String action = (String) payload.get("action");
            
            log.debug("Received WebSocket message: action={}, session={}", action, session.getId());
            
            switch (action) {
                case "subscribe" -> handleSubscription(session, payload);
                case "unsubscribe" -> handleUnsubscription(session, payload);
                case "ping" -> handlePing(session);
                default -> log.warn("Unknown WebSocket action: {}", action);
            }
        } catch (IOException e) {
            log.error("Error processing WebSocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String sessionId = session.getId();
        SessionMetadata metadata = sessionMetadata.remove(sessionId);
        
        if (metadata != null) {
            tenantSessions.getOrDefault(metadata.tenantId(), new ConcurrentHashMap<>())
                .remove(sessionId);
        }
        
        log.info("WebSocket connection closed: {}, status: {}", sessionId, status);
    }

    /**
     * Broadcast conference status update to all subscribed clients in tenant.
     */
    public void broadcastConferenceUpdate(String tenantId, Conference conference) {
        var sessions = tenantSessions.get(tenantId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        ConferenceStatusDto statusDto = ConferenceStatusDto.from(conference);
        
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "type", "conference_update",
                "data", statusDto,
                "timestamp", Instant.now().toEpochMilli()
            ));
            
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        log.error("Error sending message to session: {}", session.getId(), e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error serializing conference update", e);
        }
    }

    /**
     * Send participant join/leave event to monitoring clients.
     */
    public void sendParticipantEvent(String tenantId, String conferenceId, 
                                     String eventType, String participantId) {
        messagingTemplate.convertAndSend(
            "/topic/tenants/" + tenantId + "/conferences/" + conferenceId + "/participants",
            Map.of(
                "event", eventType,
                "participantId", participantId,
                "conferenceId", conferenceId,
                "timestamp", Instant.now().toEpochMilli()
            )
        );
    }

    /**
     * Broadcast JVB node status changes.
     */
    public void broadcastJvbStatus(String tenantId, String nodeId, boolean healthy, 
                                   int participantCount, double cpuLoad) {
        messagingTemplate.convertAndSend(
            "/topic/tenants/" + tenantId + "/jvb/status",
            Map.of(
                "nodeId", nodeId,
                "healthy", healthy,
                "participantCount", participantCount,
                "cpuLoad", cpuLoad,
                "timestamp", Instant.now().toEpochMilli()
            )
        );
    }

    /**
     * Broadcast recording status change.
     */
    public void broadcastRecordingStatus(String tenantId, String conferenceId, 
                                         String recordingStatus, String recordingId) {
        messagingTemplate.convertAndSend(
            "/topic/tenants/" + tenantId + "/conferences/" + conferenceId + "/recording",
            Map.of(
                "status", recordingStatus,
                "recordingId", recordingId,
                "conferenceId", conferenceId,
                "timestamp", Instant.now().toEpochMilli()
            )
        );
    }

    /**
     * Scheduled task to send heartbeat and clean up stale sessions.
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void sendHeartbeatAndCleanup() {
        Instant now = Instant.now();
        
        sessionMetadata.entrySet().removeIf(entry -> {
            SessionMetadata metadata = entry.getValue();
            if (now.isAfter(metadata.lastActivity().plusSeconds(120))) {
                try {
                    WebSocketSession session = tenantSessions
                        .getOrDefault(metadata.tenantId(), new ConcurrentHashMap<>())
                        .get(entry.getKey());
                    
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                    log.info("Closed stale WebSocket session: {}", entry.getKey());
                } catch (IOException e) {
                    log.error("Error closing stale session", e);
                }
                return true;
            }
            return false;
        });
        
        // Send heartbeat to active sessions
        tenantSessions.values().forEach(sessions -> 
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(
                            "{\"type\":\"heartbeat\",\"timestamp\":" + now.toEpochMilli() + "}"
                        ));
                    } catch (IOException e) {
                        log.warn("Failed to send heartbeat to session: {}", session.getId());
                    }
                }
            })
        );
    }

    private void handleSubscription(WebSocketSession session, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        String tenantId = extractTenantId(session);
        
        if (room != null) {
            String destination = "/topic/tenants/" + tenantId + "/conferences/" + room;
            log.debug("Session {} subscribed to {}", session.getId(), destination);
        }
    }

    private void handleUnsubscription(WebSocketSession session, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        log.debug("Session {} unsubscribed from room: {}", session.getId(), room);
    }

    private void handlePing(WebSocketSession session) throws IOException {
        sessionMetadata.computeIfPresent(session.getId(), (k, v) -> 
            new SessionMetadata(v.tenantId(), Instant.now()));
        
        session.sendMessage(new TextMessage(
            "{\"type\":\"pong\",\"timestamp\":" + Instant.now().toEpochMilli() + "}"
        ));
    }

    private String extractTenantId(WebSocketSession session) {
        // Extract from URL params or session attributes
        String tenantId = session.getUri().getQuery();
        if (tenantId != null && tenantId.contains("tenantId=")) {
            return tenantId.split("tenantId=")[1].split("&")[0];
        }
        return "default";
    }

    private record SessionMetadata(String tenantId, Instant lastActivity) {}
}
