package beordie.cn.notification.example;

import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import beordie.cn.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class NotificationExample {
    
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationExample(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public Mono<NotificationResult> sendFeishuNotification() {
        Notification notification = new Notification();
        notification.setTitle("任务提醒");
        notification.setContent("您有一个待办事项即将到期，请及时处理！");
        notification.setType(NotificationType.FEISHU);
        
        return notificationService.send(notification);
    }
    
    public Mono<NotificationResult> sendEmailNotification() {
        return notificationService.sendEmail(
                "周报提醒",
                "请在本周五前提交本周工作总结",
                "user@example.com"
        );
    }
    
    public Mono<NotificationResult> sendMultiChannelNotification() {
        Notification notification = new Notification(
                "紧急通知",
                "系统将于今晚22:00进行维护，请提前保存数据",
                "admin@example.com",
                null
        );
        
        List<NotificationType> channels = Arrays.asList(
                NotificationType.FEISHU,
                NotificationType.EMAIL
        );
        
        return notificationService.sendToMultiple(notification, channels);
    }
    
    public Mono<NotificationResult> sendFeishuWithExtra() {
        Notification notification = new Notification();
        notification.setTitle("自定义通知");
        notification.setContent("这是一条带有额外信息的通知");
        notification.setType(NotificationType.FEISHU);
        notification.addExtra("webhookUrl", "https://custom-webhook.example.com");
        notification.addExtra("priority", "high");
        
        return notificationService.send(notification);
    }
}
