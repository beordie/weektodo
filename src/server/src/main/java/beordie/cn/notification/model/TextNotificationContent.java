package beordie.cn.notification.model;

import beordie.cn.notification.adapter.FeishuMessageBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextNotificationContent extends NotificationContent implements FeishuMessageBuilder {
    private static final Logger log = LoggerFactory.getLogger(TextNotificationContent.class);
    
    private String title;
    private String content;

    public TextNotificationContent() {
        super();
    }

    public TextNotificationContent(String title, String content, String startTime, String endTime) {
        super(startTime, endTime);
        this.title = title;
        this.content = content;
    }

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public Map<String, Object> buildFeishuMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "text");
        
        InnerTextNotificationContent innerContent = new InnerTextNotificationContent();
        innerContent.setTitle(content);
        innerContent.setStartTime(super.getStartTime());
        innerContent.setEndTime(super.getEndTime());
        message.put("content", innerContent);
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            log.info("Building Feishu message:\n{}", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert message to JSON", e);
            log.info("Building Feishu message: message={}", message);
        }
        
        return message;
    }

    @Data
    static class InnerTextNotificationContent {
        private String title;
        private String startTime;
        private String endTime;
    }
}
