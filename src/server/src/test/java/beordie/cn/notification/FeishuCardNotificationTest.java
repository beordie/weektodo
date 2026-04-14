package beordie.cn.notification;

import beordie.cn.notification.adapter.impl.FeishuNotificationAdapter;
import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FeishuCardNotificationTest {
    
    @Autowired
    private FeishuNotificationAdapter feishuNotificationAdapter;
    
    @Test
    public void testSendFeishuCardNotification() {
        Notification notification = new Notification();
        notification.setType(NotificationType.FEISHU);
        
        Map<String, Object> card = new HashMap<>();
        card.put("schema", "2.0");
        
        Map<String, Object> config = new HashMap<>();
        config.put("update_multi", true);
        Map<String, Object> style = new HashMap<>();
        Map<String, Object> textSize = new HashMap<>();
        Map<String, Object> normalV2 = new HashMap<>();
        normalV2.put("default", "normal");
        normalV2.put("pc", "normal");
        normalV2.put("mobile", "heading");
        textSize.put("normal_v2", normalV2);
        style.put("text_size", textSize);
        config.put("style", style);
        card.put("config", config);
        
        Map<String, Object> header = new HashMap<>();
        Map<String, Object> title = new HashMap<>();
        title.put("tag", "plain_text");
        title.put("content", "今日旅游推荐");
        header.put("title", title);
        Map<String, Object> subtitle = new HashMap<>();
        subtitle.put("tag", "plain_text");
        subtitle.put("content", "");
        header.put("subtitle", subtitle);
        header.put("template", "blue");
        header.put("padding", "12px 12px 12px 12px");
        card.put("header", header);
        
        Map<String, Object> body = new HashMap<>();
        body.put("direction", "vertical");
        body.put("padding", "12px 12px 12px 12px");
        
        java.util.List<Map<String, Object>> elements = new java.util.ArrayList<>();
        
        Map<String, Object> markdownElement = new HashMap<>();
        markdownElement.put("tag", "markdown");
        markdownElement.put("content", "西湖，位于中国浙江省杭州市西湖区龙井路1号，杭州市区西部，汇水面积为21.22平方千米，湖面面积为6.38平方千米。");
        markdownElement.put("text_align", "left");
        markdownElement.put("text_size", "normal_v2");
        markdownElement.put("margin", "0px 0px 0px 0px");
        elements.add(markdownElement);
        
        Map<String, Object> buttonElement = new HashMap<>();
        buttonElement.put("tag", "button");
        Map<String, Object> buttonText = new HashMap<>();
        buttonText.put("tag", "plain_text");
        buttonText.put("content", "🌞更多景点介绍");
        buttonElement.put("text", buttonText);
        buttonElement.put("type", "default");
        buttonElement.put("width", "default");
        buttonElement.put("size", "medium");
        
        java.util.List<Map<String, Object>> behaviors = new java.util.ArrayList<>();
        Map<String, Object> behavior = new HashMap<>();
        behavior.put("type", "open_url");
        behavior.put("default_url", "https://baike.baidu.com/item/西湖/4668821");
        behavior.put("pc_url", "");
        behavior.put("ios_url", "");
        behavior.put("android_url", "");
        behaviors.add(behavior);
        buttonElement.put("behaviors", behaviors);
        buttonElement.put("margin", "0px 0px 0px 0px");
        elements.add(buttonElement);
        
        body.put("elements", elements);
        card.put("body", body);
        
        notification.addExtra("card", card);
        
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
