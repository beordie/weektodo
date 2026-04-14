package beordie.cn.notification.adapter.impl;

import beordie.cn.notification.adapter.NotificationAdapter;
import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

public class EmailNotificationAdapter implements NotificationAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Autowired
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    public EmailNotificationAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }
    
    @Override
    public Mono<NotificationResult> send(Notification notification) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("Email sender not configured, skipping notification");
            return Mono.just(NotificationResult.failure("Email sender not configured"));
        }
        
        if (notification.getRecipient() == null || notification.getRecipient().isBlank()) {
            return Mono.just(NotificationResult.failure("Email recipient is required"));
        }
        
        return Mono.fromCallable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(notification.getRecipient());
                message.setSubject(notification.getTitle());
                message.setText(notification.getContent());
                
                mailSender.send(message);
                
                String messageId = UUID.randomUUID().toString();
                log.info("Email notification sent successfully to {}: {}", 
                        notification.getRecipient(), messageId);
                
                NotificationResult result = NotificationResult.success(messageId, "Email sent");
                result.setType(NotificationType.EMAIL);
                return result;
            } catch (Exception ex) {
                log.error("Failed to send email notification", ex);
                return NotificationResult.failure(ex.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
