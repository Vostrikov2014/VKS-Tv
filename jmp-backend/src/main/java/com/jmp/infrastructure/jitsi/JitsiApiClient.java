package com.jmp.infrastructure.jitsi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with Jitsi External API.
 * Provides methods for conference management, participant control, and status retrieval.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JitsiApiClient {

    private final JitsiProperties jitsiProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public JitsiApiClient(JitsiProperties jitsiProperties, ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
        this.jitsiProperties = jitsiProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(jitsiProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(jitsiProperties.getReadTimeout()))
                .build();
    }

    /**
     * Gets the number of participants in a conference room.
     *
     * @param roomId The conference room ID
     * @return Number of participants, or -1 if error
     */
    public int getParticipantCount(String roomId) {
        try {
            String url = buildApiUrl("/about/statistics");
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            
            if (response.getBody() != null && response.getBody().has("participants")) {
                return response.getBody().get("participants").asInt(-1);
            }
        } catch (Exception e) {
            log.error("Failed to get participant count for room: {}", roomId, e);
        }
        return -1;
    }

    /**
     * Gets detailed statistics about a Jitsi instance.
     */
    public Map<String, Object> getStatistics() {
        try {
            String url = buildApiUrl("/about/statistics");
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            
            if (response.getBody() != null) {
                return objectMapper.convertValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
        }
        return new HashMap<>();
    }

    /**
     * Checks if the Jitsi API is healthy.
     */
    public boolean isHealthy() {
        try {
            String url = buildApiUrl("/health");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Jitsi health check failed", e);
            return false;
        }
    }

    /**
     * Sends a command to disconnect a participant from a conference.
     * Note: This requires Jitsi to have the HTTP API enabled.
     *
     * @param roomId The conference room ID
     * @param participantId The participant's session ID
     * @return true if successful
     */
    public boolean disconnectParticipant(String roomId, String participantId) {
        try {
            String url = buildApiUrl("/conference/" + roomId + "/participants/" + participantId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to disconnect participant {} from room {}", participantId, roomId, e);
            return false;
        }
    }

    /**
     * Sends a message to all participants in a conference.
     *
     * @param roomId The conference room ID
     * @param message The message to send
     * @return true if successful
     */
    public boolean sendMessageToRoom(String roomId, String message) {
        try {
            String url = buildApiUrl("/conference/" + roomId + "/message");
            
            Map<String, String> payload = new HashMap<>();
            payload.put("message", message);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to send message to room {}", roomId, e);
            return false;
        }
    }

    /**
     * Locks a conference room (prevents new participants from joining).
     */
    public boolean lockRoom(String roomId) {
        return setRoomLock(roomId, true);
    }

    /**
     * Unlocks a conference room.
     */
    public boolean unlockRoom(String roomId) {
        return setRoomLock(roomId, false);
    }

    /**
     * Sets the lock state of a conference room.
     */
    private boolean setRoomLock(String roomId, boolean locked) {
        try {
            String url = buildApiUrl("/conference/" + roomId + "/lock");
            
            Map<String, Boolean> payload = new HashMap<>();
            payload.put("locked", locked);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Boolean>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to set room lock for room {}", roomId, e);
            return false;
        }
    }

    /**
     * Builds the full API URL from the base URL and path.
     */
    private String buildApiUrl(String path) {
        String baseUrl = jitsiProperties.getApiUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://" + jitsiProperties.getDomain();
        }
        
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(path)
                .build()
                .toUriString();
    }
}
