package beordie.cn.dashboard.condition;

import java.util.Collections;
import java.util.Map;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EnabledMetricCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnabledMetric.class.getName());
        if (attrs == null) return true;
        String id = (String) attrs.get("value");
        Binder binder = Binder.get(context.getEnvironment());
        Map<String, Object> metrics = binder.bind("app.dashboard.metrics", Bindable.mapOf(String.class, Object.class))
                .orElse(Collections.emptyMap());
        if (metrics == null || metrics.isEmpty()) return true;
        return metrics.containsKey(id);
    }
}

