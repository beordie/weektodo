package beordie.cn.dashboard;

import beordie.cn.dashboard.dto.DashboardStat;

public interface DashboardMetricCalculator {
    String id();
    boolean supports(DashboardScope scope);
    int order();
    DashboardStat calculate(DashboardContext ctx);
}

