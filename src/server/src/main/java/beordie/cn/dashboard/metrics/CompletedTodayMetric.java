package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardFooter;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.model.Todo;
import lombok.NonNull;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import beordie.cn.handler.ConfigHandler.DashboardFooterConfig;
import beordie.cn.utils.TemplateEngineUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("completed_today")
@Conditional(EnabledMetricCondition.class)
public class CompletedTodayMetric extends AbstractDashboardMetricCalculator {
    public CompletedTodayMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "completed_today"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = super.getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 80;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int completedToday = 0;
        int completedYestoday = 0;
        
        // 如果是TaskDashboardContext，尝试统计今天完成的todos
        if (ctx instanceof TaskDashboardContext taskCtx) {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = LocalDate.now().minusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
            
            String todayFormatted = today.format(formatter);
            String yesterdayFormatted = yesterday.format(formatter);
            
            for (Todo todo : taskCtx.getTodos()) {
                // 检查是否已完成，并且更新时间在今天
                if (!todo.checkCompleted()) {
                    continue;
                }

                if (todayFormatted.equals(todo.getListId())) {
                    completedToday++;
                } else if (yesterdayFormatted.equals(todo.getListId())) {
                    completedYestoday++;
                }
            }
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = super.getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(completedToday);

        DashboardFooter footer = new DashboardFooter();
        String footerType = completedToday > completedYestoday ? "increase" : (completedToday < completedYestoday ? "decrease" : "normal");
        footer = getDashboardFooter(cfg, Math.abs(completedYestoday - completedToday), footerType);
        stat.setFooter(footer);
        return stat;
    }

    private static DashboardFooter getDashboardFooter(@NonNull DashboardMetricConfig cfg, int diff, String footerType) {
        DashboardFooterConfig f = cfg.getFooter(footerType);
        if (f == null) {
            return null;
        }

        DashboardFooter footer = new DashboardFooter();
        footer.setType(f.getType());
        footer.setText(TemplateEngineUtil.format(f.getTemplate(), "diff", diff));
        footer.setIcon(f.getIcon());
        footer.setColor(f.getColor());
        return footer;
    }
}