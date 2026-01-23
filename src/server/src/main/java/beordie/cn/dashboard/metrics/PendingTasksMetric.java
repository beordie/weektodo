package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardFooter;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.model.Todo;
import beordie.cn.handler.ConfigHandler.DashboardFooterConfig;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import beordie.cn.handler.ConfigHandler.TaskTimeConfig;
import beordie.cn.utils.TemplateEngineUtil;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("pending")
@Conditional(EnabledMetricCondition.class)
public class PendingTasksMetric extends AbstractDashboardMetricCalculator {
    public PendingTasksMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "pending"; }
    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 30;
    }

    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int pending = 0, overdue = 0;
        TaskTimeConfig timeConfig = ctx.getTimeConfig();

        // 如果是TaskDashboardContext，使用todos进行统计
        if (ctx instanceof TaskDashboardContext taskCtx) {
            for (Todo todo : taskCtx.getTodos()) {
                // 待处理的todos是指checked为0或null的todos
                if (!todo.checkCompleted()) {
                    pending++;
                }
                if (todo.checkOverdue(timeConfig.getOverdueThresholdSeconds())) {
                    overdue++;
                }
            }
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(pending);
        DashboardFooter footer = getDashboardFooter(cfg, overdue);
        stat.setFooter(footer);
        return stat;
    }

    private static DashboardFooter getDashboardFooter(@NonNull DashboardMetricConfig cfg, int diff) {
        DashboardFooterConfig f = cfg.getFooter("overdue");
        DashboardFooter footer = new DashboardFooter();
        footer.setType(f.getType());
        footer.setText(TemplateEngineUtil.format(f.getTemplate(), "count", Math.abs(diff)));
        footer.setIcon(f.getIcon());
        footer.setColor(f.getColor());
        return footer;
    }
}
