package beordie.cn.dashboard.milestone;

public class Item {
    /**
     * 里程碑名称
     */
    private String name;
    /**
     * 总任务数
     */
    private Integer totalTodos;
    /**
     * 已完成的待办事项数
     */
    private Integer completedTodos;
    /**
     * 待处理的待办事项数
     */
    private Integer pendingTodos;
    /**
     * 完成率（百分比）
     */
    private Double completionRate;

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalTodos() {
        return totalTodos;
    }

    public void setTotalTodos(Integer totalTodos) {
        this.totalTodos = totalTodos;
    }

    public Integer getCompletedTodos() {
        return completedTodos;
    }

    public void setCompletedTodos(Integer completedTodos) {
        this.completedTodos = completedTodos;
    }

    public Integer getPendingTodos() {
        return pendingTodos;
    }

    public void setPendingTodos(Integer pendingTodos) {
        this.pendingTodos = pendingTodos;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
}
