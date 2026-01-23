package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.model.Task;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("total_todo_time")
@Conditional(EnabledMetricCondition.class)
public class TotalTodoTimeMetric extends AbstractDashboardMetricCalculator {
    public TotalTodoTimeMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "total_todo_time"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 60;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        double total = 0.0;
        for (Task task : ctx.getTasks()) {
            total += ctx.getTimeCacheService().getTaskTotalTimeInHours(task.getId()).blockOptional().orElse(0.0);;
        }

        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(total);
        return stat;
    }
}
