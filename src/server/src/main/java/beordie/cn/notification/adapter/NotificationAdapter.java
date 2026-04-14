package beordie.cn.notification.adapter;

import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import reactor.core.publisher.Mono;

public interface NotificationAdapter {
    
    NotificationType getType();
    
    Mono<NotificationResult> send(Notification notification);
    
    default boolean supports(NotificationType type) {
        return getType() == type;
    }
}
