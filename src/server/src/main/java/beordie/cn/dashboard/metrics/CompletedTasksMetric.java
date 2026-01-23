package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardFooter;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.model.Task;
import beordie.cn.model.Todo;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import beordie.cn.handler.ConfigHandler.DashboardFooterConfig;
import beordie.cn.utils.TemplateEngineUtil;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("completed")
@Conditional(EnabledMetricCondition.class)
public class CompletedTasksMetric extends AbstractDashboardMetricCalculator {
    public CompletedTasksMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "completed"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = super.getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 20;
    }



    public DashboardStat calculate(DashboardContext ctx) {
        int completed = 0;
        double totalHours = 0.0;
        String footerType = "";

        // 如果是TaskDashboardContext，使用todos进行统计
        if (ctx instanceof TaskDashboardContext taskCtx) {
            for (Todo todo : taskCtx.getTodos()) {
                if (todo.checkCompleted()) {
                    completed++;
                    double hours = ctx.getTimeCacheService().getTodoTotalTimeInHours(todo.getId()).blockOptional().orElse(0.0);
                    totalHours += hours;
                }
            }
            footerType = "text";
        } else {
            // 原始逻辑，使用tasks进行统计
            for (Task task : ctx.getTasks()) {
                if (task.checkCompleted()) {
                    completed++;
                }
            }
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(completed);
        DashboardFooter footer = getDashboardFooter(cfg, totalHours, footerType);
        stat.setFooter(footer);
        return stat;
    }

    private static DashboardFooter getDashboardFooter(@NonNull DashboardMetricConfig cfg, double totalHours, String footerType) {
        DashboardFooterConfig f = cfg.getFooter(footerType);
        if (f == null) {
            return null;
        }

        DashboardFooter footer = new DashboardFooter();
        footer.setType(f.getType());
        footer.setText(TemplateEngineUtil.format(f.getTemplate(), "hours", totalHours));
        footer.setIcon(f.getIcon());
        footer.setColor(f.getColor());
        return footer;
    }
}
