package beordie.cn.notification.adapter.impl;

import beordie.cn.notification.adapter.FeishuMessageBuilder;
import beordie.cn.notification.adapter.NotificationAdapter;
import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import beordie.cn.notification.model.TextNotificationContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FeishuNotificationAdapter implements NotificationAdapter {
    
    private static final Logger log = LoggerFactory.getLogger(FeishuNotificationAdapter.class);
    
    private final WebClient webClient;
    
    @Value("${notification.feishu.webhook-url:}")
    private String webhookUrl;
    
    @Value("${notification.feishu.secret:}")
    private String secret;
    
    public FeishuNotificationAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    @Override
    public NotificationType getType() {
        return NotificationType.FEISHU;
    }
    
    @Override
    public Mono<NotificationResult> send(Notification notification) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Feishu webhook URL not configured, skipping notification");
            return Mono.just(NotificationResult.failure("Feishu webhook URL not configured"));
        }
        
        Map<String, Object> message = buildMessage(notification);
        
        return webClient.post()
                .uri(webhookUrl)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object code = response.get("code");
                    if (code instanceof Number && ((Number) code).intValue() == 0) {
                        String messageId = UUID.randomUUID().toString();
                        log.info("Feishu notification sent successfully: {}", messageId);
                        NotificationResult result = NotificationResult.success(messageId, "Notification sent");
                        result.setType(NotificationType.FEISHU);
                        return result;
                    } else {
                        String errorMsg = (String) response.get("msg");
                        log.error("Failed to send Feishu notification: {}", errorMsg);
                        return NotificationResult.failure(errorMsg != null ? errorMsg : "Unknown error");
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Error sending Feishu notification", ex);
                    return Mono.just(NotificationResult.failure(ex.getMessage()));
                });
    }
    
    private Map<String, Object> buildMessage(Notification notification) {
        TextNotificationContent textContent = 
            new TextNotificationContent(
                notification.getTitle(),
                notification.getContent(),
                notification.getStartTime(),
                notification.getEndTime()
            );
        return textContent.buildFeishuMessage();
    }
}
