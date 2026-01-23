package beordie.cn.dashboard.metrics;

import beordie.cn.dashboard.AbstractDashboardMetricCalculator;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.dashboard.dto.DashboardFooter;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import beordie.cn.handler.ConfigHandler.DashboardFooterConfig;
import beordie.cn.model.Todo;
import beordie.cn.utils.TemplateEngineUtil;
import lombok.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Conditional;
import beordie.cn.dashboard.condition.EnabledMetric;
import beordie.cn.dashboard.condition.EnabledMetricCondition;

@Component
@EnabledMetric("new_this_week")
@Conditional(EnabledMetricCondition.class)
public class NewThisWeekMetric extends AbstractDashboardMetricCalculator {
    public NewThisWeekMetric(@Autowired ConfigHandler configHandler) {
        super(configHandler);
    }

    @Override
    public String id() { return "new_this_week"; }
    
    @Override
    public int order() {
        DashboardMetricConfig cfg = getCfg();
        if (cfg.getOrder() != null) return cfg.getOrder();
        return 70;
    }
    
    @Override
    public DashboardStat calculate(DashboardContext ctx) {
        int newThisWeek = 0;
        int newLastWeek = 0;
        LocalDate nowDate = ctx.getNow().toLocalDate();
        LocalDate thisWeekStart = nowDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        if (ctx instanceof TaskDashboardContext taskCtx) {
            for (Todo todo : taskCtx.getTodos()) {
                LocalDate date = todo.getDate();
                if (date.isAfter(thisWeekStart)) {
                    newThisWeek++;
                } else if (date.isBefore(thisWeekStart) && date.isAfter(lastWeekStart)) {
                    newLastWeek++;
                }
            }
        }
        
        DashboardStat stat = new DashboardStat();
        stat.setId(id());
        DashboardMetricConfig cfg = getCfg();
        stat.setTitle(cfg.getTitle());
        stat.setValue(newThisWeek);
        int diff = newThisWeek - newLastWeek;
        String footerType = diff > 0 ? "increase" : (diff < 0 ? "decrease" : "normal");
        DashboardFooter footer = getDashboardFooter(cfg, Math.abs(diff), footerType);
        stat.setFooter(footer);
        return stat;
    }

    private static DashboardFooter getDashboardFooter(@NonNull DashboardMetricConfig cfg, int diff, String footerType) {
        DashboardFooterConfig f = cfg.getFooter(footerType);
        DashboardFooter footer = new DashboardFooter();
        footer.setType(f.getType());
        footer.setText(TemplateEngineUtil.format(f.getTemplate(), "diff", Math.abs(diff)));
        footer.setIcon(f.getIcon());
        footer.setColor(f.getColor());
        return footer;
    }
}