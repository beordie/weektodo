package beordie.cn.dashboard.milestone;

public class Summary {
    /**
     * 里程碑总数
     */
    private int total;
    /**
     * 平均完成率
     */
    private double averageCompletionRate;

    // Getter and Setter methods
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getAverageCompletionRate() {
        return averageCompletionRate;
    }

    public void setAverageCompletionRate(double averageCompletionRate) {
        this.averageCompletionRate = averageCompletionRate;
    }
}
