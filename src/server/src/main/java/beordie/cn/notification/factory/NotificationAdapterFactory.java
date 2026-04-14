package beordie.cn.notification.factory;

import beordie.cn.notification.adapter.NotificationAdapter;
import beordie.cn.notification.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationAdapterFactory {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationAdapterFactory.class);
    
    private final Map<NotificationType, NotificationAdapter> adapters = new HashMap<>();
    
    public NotificationAdapterFactory(List<NotificationAdapter> adapterList) {
        for (NotificationAdapter adapter : adapterList) {
            adapters.put(adapter.getType(), adapter);
            log.info("Registered notification adapter: {}", adapter.getType());
        }
    }
    
    public NotificationAdapter getAdapter(NotificationType type) {
        NotificationAdapter adapter = adapters.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter found for notification type: " + type);
        }
        return adapter;
    }
    
    public boolean hasAdapter(NotificationType type) {
        return adapters.containsKey(type);
    }
}
