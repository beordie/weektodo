package beordie.cn.notification.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextNotificationContent.class, name = "text"),
    @JsonSubTypes.Type(value = CardNotificationContent.class, name = "card")
})
public abstract class NotificationContent {
    private String startTime;
    private String endTime;

    public NotificationContent() {}

    public NotificationContent(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public abstract String getType();
}
