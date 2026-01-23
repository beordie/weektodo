package beordie.cn.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("task")
public class Task implements CheckStatus {

    @Override
    public boolean checkCompleted() {
        return completed != null && completed == 1;
    }

    @Override
    public boolean checkOverdue(long overdueThreshold) {
        // 如果任务已完成，则不逾期
        if (checkCompleted()) {
            return false;
        }
        
        if (endDate == null) {
            return false;
        }
        
        // 获取当前日期
        LocalDate nowDate = LocalDate.now();
        
        // 计算当前日期与结束日期的天数差
        // 如果endDate在nowDate之前，差为正数
        long daysDiff = nowDate.toEpochDay() - endDate.toEpochDay();
        
        // 如果天数差大于overdueDay，则任务逾期
        return daysDiff > overdueThreshold;
    }
    /**
     * 任务ID，UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 任务标题
     */
    private String title;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务开始日期
     */
    private LocalDate startDate;
    
    /**
     * 任务结束日期
     */
    private LocalDate endDate;
    
    /**
     * 任务分类
     */
    private String category;
    
    /**
     * 任务优先级
     */
    private Integer priority;
    
    /**
     * 任务完成状态
     */
    private Integer completed;
    
    /**
     * 任务关联的待办事项列表，数据库中不存在，通过关联查询获取
     */
    @TableField(exist = false)
    private List<Todo> todos;
    
    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 任务最后更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 任务颜色标记
     */
    private String color;
    
    /**
     * 任务关联的里程碑列表，数据库中不存在，通过关联查询获取
     */
    @TableField(exist = false)
    private List<Milestone> milestones;

    /**
     * 任务关联的里程碑完成情况
     */
    @TableField(exist = false)
    private MilestoneCounter milestoneCounter;

    // 构造函数
    public Task() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getCompleted() {
        return completed;
    }
    
    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public MilestoneCounter getMilestoneCounter() {
        return milestoneCounter;
    }

    public void setMilestoneCounter(MilestoneCounter milestoneCounter) {
        this.milestoneCounter = milestoneCounter;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + "'" +
                ", title='" + title + "'" +
                ", description='" + description + "'" +
                ", startDate='" + startDate + "'" +
                ", endDate='" + endDate + "'" +
                ", category='" + category + "'" +
                ", priority='" + priority + "'" +
                ", completed=" + completed +
                ", todos=" + todos +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", color='" + color + "'" +
                ", milestones=" + milestones +
                '}';
    }

    public static class MilestoneCounter {
        private Integer total;
        private Integer done;

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getDone() {
            return done;
        }

        public void setDone(Integer done) {
            this.done = done;
        }
    }
}
