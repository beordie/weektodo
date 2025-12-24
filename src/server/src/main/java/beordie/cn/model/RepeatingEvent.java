package beordie.cn.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重复事件模型
 */
@TableName("repeating_event")
public class RepeatingEvent {
    /**
     * 重复事件ID，唯一标识符
     */
    @TableId
    private String id;
    
    /**
     * 重复事件的开始日期
     */
    private Date startDate;
    
    /**
     * 重复规则，定义事件的重复模式（如每天、每周、每月等）
     */
    private String repeatingRule;
    
    /**
     * 重复事件的类型
     */
    private String type;
    
    /**
     * 重复发生的类型，与重复规则相关
     */
    private String occurrencesType;
    
    /**
     * 重复事件关联的 todoId, 实际上就是重复的创建该 todo
     */
    private String todoId;

    /**
     * 重复事件关联的 todo, 实际上就是重复的创建该 todo
     */
    @TableField(exist = false)
    private Todo todo;
    
    /**
     * 重复事件的结束日期，超过此日期后事件不再重复
     */
    private Date endDate;
    
    /**
     * 重复事件的创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 重复事件的最后更新时间
     */
    private LocalDateTime updatedAt;

    // Getter and Setter methods for RepeatingEvent class
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getRepeatingRule() {
        return repeatingRule;
    }

    public void setRepeatingRule(String repeatingRule) {
        this.repeatingRule = repeatingRule;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOccurrencesType() {
        return occurrencesType;
    }

    public void setOccurrencesType(String occurrencesType) {
        this.occurrencesType = occurrencesType;
    }

    public String getTodoId() {
        return todoId;
    }

    public void setTodoId(String todoId) {
        this.todoId = todoId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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


    // Method to get all data as a Map for generating Todo
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        return data;
    }
}
