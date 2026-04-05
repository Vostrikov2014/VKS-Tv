package com.jmp.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email notification service for conference events and system alerts.
 * Implements async sending, templates, and i18n support.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${jmp.mail.from-address:noreply@jmp.platform}")
    private String fromAddress;

    @Value("${jmp.mail.base-url:https://jmp.platform}")
    private String baseUrl;

    /**
     * Send conference invitation email.
     */
    @Async
    public void sendConferenceInvitation(String to, String conferenceName, 
                                         String joinUrl, String scheduledTime) {
        log.info("Sending conference invitation to: {} for: {}", to, conferenceName);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Invitation: " + conferenceName);
            
            String htmlContent = buildInvitationEmail(conferenceName, joinUrl, scheduledTime);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Conference invitation sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send conference invitation to: {}", to, e);
            throw new NotificationException("Failed to send invitation", e);
        }
    }

    /**
     * Send recording ready notification.
     */
    @Async
    public void sendRecordingReady(String to, String conferenceName, 
                                   String downloadUrl, long durationSeconds) {
        log.info("Sending recording ready notification to: {} for: {}", to, conferenceName);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Recording Ready: " + conferenceName);
            
            String htmlContent = buildRecordingReadyEmail(conferenceName, downloadUrl, durationSeconds);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Recording ready notification sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send recording notification to: {}", to, e);
            throw new NotificationException("Failed to send recording notification", e);
        }
    }

    /**
     * Send password reset email.
     */
    @Async
    public void sendPasswordReset(String to, String username, String resetToken) {
        log.info("Sending password reset email to: {}", to);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Password Reset Request");
            
            String resetUrl = baseUrl + "/auth/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmail(username, resetUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new NotificationException("Failed to send password reset email", e);
        }
    }

    /**
     * Send welcome email to new users.
     */
    @Async
    public void sendWelcomeEmail(String to, String username, String tenantName) {
        log.info("Sending welcome email to: {}", to);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Welcome to Jitsi Management Platform");
            
            String htmlContent = buildWelcomeEmail(username, tenantName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            throw new NotificationException("Failed to send welcome email", e);
        }
    }

    /**
     * Send system alert to administrators.
     */
    @Async
    public void sendSystemAlert(String to, String subject, String message, String severity) {
        log.info("Sending system alert to: {} - severity: {}", to, severity);
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromAddress);
            mailMessage.setTo(to);
            mailMessage.setSubject("[ALERT-" + severity + "] " + subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            log.info("System alert sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Failed to send system alert to: {}", to, e);
            throw new NotificationException("Failed to send system alert", e);
        }
    }

    /**
     * Send meeting reminder 15 minutes before start.
     */
    @Async
    public void sendMeetingReminder(String to, String conferenceName, 
                                    String joinUrl, String startTime) {
        log.info("Sending meeting reminder to: {} for: {}", to, conferenceName);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Reminder: " + conferenceName + " starts in 15 minutes");
            
            String htmlContent = buildReminderEmail(conferenceName, joinUrl, startTime);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Meeting reminder sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send meeting reminder to: {}", to, e);
            throw new NotificationException("Failed to send meeting reminder", e);
        }
    }

    private String buildInvitationEmail(String conferenceName, String joinUrl, String scheduledTime) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #1a73e8; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background: #1a73e8; 
                              color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Conference Invitation</h1>
                    </div>
                    <div class="content">
                        <p>You have been invited to join the conference:</p>
                        <h2>%s</h2>
                        <p><strong>Scheduled Time:</strong> %s</p>
                        <a href="%s" class="button">Join Conference</a>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all;">%s</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message from Jitsi Management Platform.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(conferenceName, scheduledTime, joinUrl, joinUrl);
    }

    private String buildRecordingReadyEmail(String conferenceName, String downloadUrl, long durationSeconds) {
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #34a853; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background: #34a853; 
                              color: white; text-decoration: none; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Recording Ready</h1>
                    </div>
                    <div class="content">
                        <p>The recording for <strong>%s</strong> is now available.</p>
                        <p><strong>Duration:</strong> %d:%02d</p>
                        <a href="%s" class="button">Download Recording</a>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(conferenceName, minutes, seconds, downloadUrl);
    }

    private String buildPasswordResetEmail(String username, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <p>Hello %s,</p>
                <p>You requested a password reset. Click the link below to reset your password:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link will expire in 1 hour.</p>
                <p>If you didn't request this, please ignore this email.</p>
            </body>
            </html>
            """.formatted(username, resetUrl);
    }

    private String buildWelcomeEmail(String username, String tenantName) {
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Welcome to Jitsi Management Platform!</h1>
                <p>Hello %s,</p>
                <p>Your account has been created for organization: <strong>%s</strong></p>
                <p>You can now start managing video conferences.</p>
                <p>Best regards,<br/>The JMP Team</p>
            </body>
            </html>
            """.formatted(username, tenantName);
    }

    private String buildReminderEmail(String conferenceName, String joinUrl, String startTime) {
        return """
            <!DOCTYPE html>
            <html>
            <body>
                <h2>Meeting Reminder</h2>
                <p>Your conference <strong>%s</strong> starts in 15 minutes.</p>
                <p><strong>Start Time:</strong> %s</p>
                <p><a href="%s">Join Now</a></p>
            </body>
            </html>
            """.formatted(conferenceName, startTime, joinUrl);
    }

    /**
     * Custom exception for notification failures.
     */
    public static class NotificationException extends RuntimeException {
        public NotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
