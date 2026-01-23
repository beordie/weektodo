package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("total_tasks")
@Conditional(EnabledMetricCondition.class)
public class TotalTasksMetric extends AbstractDashboardMetricCalculator {
    public TotalTasksMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "total_tasks"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 10;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int total = 0;
        
        // 如果是TaskDashboardContext，使用todos的总数
        if (ctx instanceof TaskDashboardContext taskCtx) {
            total = taskCtx.getTodos().size();
        } else {
            // 原始逻辑，使用tasks的总数
            total = ctx.getTasks().size();
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(total);
        return stat;
    }


}
