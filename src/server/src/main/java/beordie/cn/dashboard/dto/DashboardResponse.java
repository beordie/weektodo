package beordie.cn.dashboard.dto;

import java.util.List;

/**
 * @author: eason
 * @create: 2025-12-24
 * @Description: 看板响应数据
 */
public class DashboardResponse {
    private List<DashboardStat> dashboardStats;

    public List<DashboardStat> getDashboardStats() {
        return dashboardStats;
    }

    public void setDashboardStats(List<DashboardStat> dashboardStats) {
        this.dashboardStats = dashboardStats;
    }
}