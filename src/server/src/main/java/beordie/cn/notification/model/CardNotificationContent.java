package beordie.cn.notification.model;

import beordie.cn.notification.adapter.FeishuMessageBuilder;

import java.util.HashMap;
import java.util.Map;

public class CardNotificationContent extends NotificationContent implements FeishuMessageBuilder {
    private Map<String, Object> card;

    public CardNotificationContent() {
    }

    public CardNotificationContent(Map<String, Object> card) {
        this.card = card;
    }

    @Override
    public String getType() {
        return "card";
    }

    @Override
    public Map<String, Object> buildFeishuMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "interactive");
        message.put("card", card);
        return message;
    }

    public Map<String, Object> getCard() {
        return card;
    }

    public void setCard(Map<String, Object> card) {
        this.card = card;
    }
}
