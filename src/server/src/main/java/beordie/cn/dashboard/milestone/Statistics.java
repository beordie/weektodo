package beordie.cn.dashboard.milestone;

import java.util.List;

public class Statistics {
    /**
     * 里程碑统计信息
     */
    private Summary summary;
    /**
     * 里程碑列表
     */
    private List<Item> milestones;

    // Getter and Setter methods
    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public List<Item> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Item> milestones) {
        this.milestones = milestones;
    }
}
