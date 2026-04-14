package beordie.cn.schedule.model;

import beordie.cn.notification.model.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class ScheduledTask {
    private String id;
    private String todoId;
    private String name;
    private String description;
    private String cronExpression;
    private LocalDateTime executeTime;
    private ScheduledTaskStatus status;
    private NotificationType notificationType;
    private String notificationTitle;
    private String notificationContent;
    private String notificationRecipient;
    private String notificationStartTime;
    private String notificationEndTime;
    private Map<String, Object> extra;
    private LocalDateTime executedAt;
    private String errorMessage;

    public ScheduledTask() {
        this.id = UUID.randomUUID().toString();
        this.status = ScheduledTaskStatus.PENDING;
    }
}
