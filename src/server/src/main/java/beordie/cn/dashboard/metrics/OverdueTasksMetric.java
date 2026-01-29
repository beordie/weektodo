package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.model.Task;
import beordie.cn.model.Todo;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@EnabledMetric("overdue_tasks")
@Conditional(EnabledMetricCondition.class)
public class OverdueTasksMetric extends AbstractDashboardMetricCalculator {
    public OverdueTasksMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "overdue_tasks"; }

    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 50;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int overdue = 0;
        
        // 如果是TaskDashboardContext，使用todos进行统计
        if (ctx instanceof TaskDashboardContext taskCtx) {
            int overdueThresholdDays = taskCtx.getTimeConfig().getOverdueThresholdSeconds();
            for (Todo todo : taskCtx.getTodos()) {
                // 使用listId作为日期（格式如20251217）
                if (todo.getListId() != null) {
                    try {
                        // 检查是否未完成且已超过阈值天数
                        if (todo.checkOverdue(overdueThresholdDays)) {
                            overdue++;
                        }
                    } catch (DateTimeParseException e) {
                        // 如果日期格式解析失败，跳过该todo
                        continue;
                    }
                }
            }
        } else {
            int overdueThresholdDays = ctx.getTimeConfig().getOverdueThresholdDays();
            // 原始逻辑，使用tasks进行统计
            for (Task task : ctx.getTasks()) {
                if (task.checkOverdue(overdueThresholdDays)) {
                    overdue++;
                }
            }
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(overdue);
        return stat;
    }
}
