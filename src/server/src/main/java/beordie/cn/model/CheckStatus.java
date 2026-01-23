package beordie.cn.model;

public interface CheckStatus {
    boolean checkCompleted();
    boolean checkOverdue(long overdueThreshold);
}
