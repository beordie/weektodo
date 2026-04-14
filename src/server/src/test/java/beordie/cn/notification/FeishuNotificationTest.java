package beordie.cn.notification;

import beordie.cn.notification.adapter.impl.FeishuNotificationAdapter;
import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

@SpringBootTest
public class FeishuNotificationTest {
    
    @Autowired
    private FeishuNotificationAdapter feishuNotificationAdapter;
    
    @Test
    public void testSendFeishuNotification() {
        Notification notification = new Notification();
        notification.setType(NotificationType.FEISHU);
        notification.setTitle("测试通知");
        notification.setContent("这是一条通过定时任务发送的测试通知\n\n发送时间：" + java.time.LocalDateTime.now());
        
        Mono<NotificationResult> resultMono = feishuNotificationAdapter.send(notification);
        
        NotificationResult result = resultMono.block();
        
        System.out.println("Notification result: " + result);
        System.out.println("Success: " + result.isSuccess());
        if (result.isSuccess()) {
            System.out.println("Message ID: " + result.getMessageId());
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }
}
