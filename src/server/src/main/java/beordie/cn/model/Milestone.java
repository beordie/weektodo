package beordie.cn.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("milestone")
public class Milestone {
    /**
     * 里程碑ID，UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 里程碑标题
     */
    private String title;
    
    /**
     * 里程碑描述
     */
    private String description;
    
    /**
     * 里程碑开始日期
     */
    private LocalDate startDate;
    
    /**
     * 里程碑结束日期
     */
    private LocalDate endDate;
    
    /**
     * 里程碑完成状态
     */
    private Integer completed;
    
    /**
     * 里程碑创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 里程碑最后更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 里程碑所属任务ID，关联到Task表
     */
    private String taskId;

    // 构造函数
    public Milestone() {
    }

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "Milestone{" +
                "id='" + id + "'" +
                ", title='" + title + "'" +
                ", description='" + description + "'" +
                ", startDate='" + startDate + "'" +
                ", endDate='" + endDate + "'" +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", taskId='" + taskId + "'" +
                '}';
    }
}
