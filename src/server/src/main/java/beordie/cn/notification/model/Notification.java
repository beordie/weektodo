package beordie.cn.notification.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Notification {
    private String title;
    private String content;
    private String recipient;
    private NotificationType type;
    private NotificationContent notificationContent;
    private Map<String, Object> extra;
    private String startTime;
    private String endTime;

    public Notification() {
        this.extra = new HashMap<>();
    }

    public Notification(String title, String content, String recipient, NotificationType type) {
        this.title = title;
        this.content = content;
        this.recipient = recipient;
        this.type = type;
        this.extra = new HashMap<>();
    }

    public void addExtra(String key, Object value) {
        this.extra.put(key, value);
    }
}
