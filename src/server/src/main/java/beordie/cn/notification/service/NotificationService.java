package beordie.cn.notification.service;

import beordie.cn.notification.adapter.NotificationAdapter;
import beordie.cn.notification.factory.NotificationAdapterFactory;
import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationAdapterFactory adapterFactory;
    
    public NotificationService(NotificationAdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }
    
    public Mono<NotificationResult> send(Notification notification) {
        log.info("Sending notification: type={}, title={}, recipient={}", 
                notification.getType(), notification.getTitle(), notification.getRecipient());
        
        NotificationAdapter adapter = adapterFactory.getAdapter(notification.getType());
        return adapter.send(notification);
    }
    
    public Mono<NotificationResult> sendToMultiple(Notification notification, List<NotificationType> types) {
        return Flux.fromIterable(types)
                .flatMap(type -> {
                    Notification copy = new Notification(
                            notification.getTitle(),
                            notification.getContent(),
                            notification.getRecipient(),
                            type
                    );
                    copy.setExtra(notification.getExtra());
                    return send(copy);
                })
                .collectList()
                .map(results -> {
                    long successCount = results.stream().filter(NotificationResult::isSuccess).count();
                    log.info("Multi-channel notification complete: {}/{} successful", successCount, results.size());
                    
                    NotificationResult result = successCount > 0 
                            ? NotificationResult.success(null, String.format("%d/%d channels succeeded", successCount, results.size()))
                            : NotificationResult.failure("All channels failed");
                    return result;
                });
    }
    
    public Mono<NotificationResult> sendFeishu(String title, String content, String webhookUrl) {
        Notification notification = new Notification(title, content, null, NotificationType.FEISHU);
        if (webhookUrl != null) {
            notification.addExtra("webhookUrl", webhookUrl);
        }
        return send(notification);
    }
    
    public Mono<NotificationResult> sendEmail(String title, String content, String recipient) {
        Notification notification = new Notification(title, content, recipient, NotificationType.EMAIL);
        return send(notification);
    }
}
