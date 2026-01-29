package beordie.cn.model;

public interface CheckStatus {
    boolean checkCompleted();
    boolean checkOverdue(long overdueThreshold);
    boolean checkUpcoming(int upcomingThreshold);
}
