package beordie.cn.notification.model;

public class NotificationResult {
    private boolean success;
    private String message;
    private String messageId;
    private NotificationType type;

    public static NotificationResult success(String messageId, String message) {
        NotificationResult result = new NotificationResult();
        result.setSuccess(true);
        result.setMessageId(messageId);
        result.setMessage(message);
        return result;
    }

    public static NotificationResult failure(String message) {
        NotificationResult result = new NotificationResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
