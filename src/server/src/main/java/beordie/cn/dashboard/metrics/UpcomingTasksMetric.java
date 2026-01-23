package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.model.Task;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("upcoming_tasks")
@Conditional(EnabledMetricCondition.class)
public class UpcomingTasksMetric extends AbstractDashboardMetricCalculator {
    public UpcomingTasksMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "upcoming_tasks"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 40;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int upcoming = 0;
        int upcomingThresholdDays = ctx.getTimeConfig().getUpcomingThresholdDays();
        LocalDate nowDate = ctx.getNow().toLocalDate();
        
        for (Task t : ctx.getTasks()) {
            LocalDate end = t.getEndDate();
            if (end != null) {
                if ((t.getCompleted() == null || t.getCompleted() != 1) && end.isAfter(nowDate) && end.isBefore(nowDate.plusDays(upcomingThresholdDays))) {
                    upcoming++;
                }
            }
        }

        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(upcoming);
        return stat;
    }
}
