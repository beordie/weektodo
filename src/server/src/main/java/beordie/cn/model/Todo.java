package beordie.cn.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@TableName(value = "todo", autoResultMap = true)
public class Todo implements CheckStatus, Comparable<Todo> {
    /**
     * 待办事项ID，UUID自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 待办事项文本内容
     */
    private String text;
    
    /**
     * 待办事项完成状态
     */
    private Integer checked;
    
    /**
     * 待办事项所属日期列表ID，格式如：20251217
     */
    private String listId;
    
    /**
     * 待办事项描述
     */
    private String description;
    
    /**
     * 待办事项的子任务列表，使用JacksonTypeHandler进行JSON序列化/反序列化
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<SubTodo> subTodos;

    /**
     * 待办事项颜色标记，数据库中不存在，通过其他方式获取
     */
    @TableField(exist = false)
    private String color;
    
    /**
     * 待办事项优先级，数值越大优先级越高
     */
    private int priority;
    
    /**
     * 待办事项标签列表，使用JacksonTypeHandler进行JSON序列化/反序列化
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;
    
    /**
     * 待办事项的时间安排，使用JacksonTypeHandler进行JSON序列化/反序列化
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Time time;
    
    /**
     * 待办事项是否设置闹钟提醒，0为不需要，1为需要
     */
    private Integer alarm;
    
    /**
     * 关联的重复事件ID
     */
    private String repeatingEventId;

    /**
     * 待办事项所属任务ID，关联到Task表
     */
    private String taskId;
    
    /**
     * 待办事项所属任务信息，数据库中不存在，通过关联查询获取
     */
    @JsonIgnore
    @TableField(exist = false)
    private String task;

    /**
     * 待办事项所属里程碑ID，关联到Milestone表
     */
    private String milestoneId;
    
    /**
     * 待办事项所属里程碑信息，数据库中不存在，通过关联查询获取
     */
    @JsonIgnore
    @TableField(exist = false)
    private String milestone;

    /**
     * 待办事项创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 待办事项最后更新时间
     */
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Override
    public boolean checkCompleted() {
        return this.checked == 1;
    }
    
    @JsonIgnore
    @Override
    public boolean checkOverdue(long overdueThresholdSeconds) {
        // 如果已完成，则不逾期
        if (checkCompleted()) {
            return false;
        }
        
        // 如果没有时间信息或结束时间，则不逾期
        if (this.time == null || this.time.getEnd() == null) {
            return false;
        }
        
        try {
            // 获取当前时间
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            // 使用time.getEndTime方法获取完整的结束时间LocalDateTime
            java.time.LocalDateTime endDateTime = this.time.getEndTime(this.listId);
            
            // 检查获取结束时间是否成功
            if (endDateTime == null) {
                return false;
            }
            
            // 计算时间差（秒）
            long diffSeconds = java.time.Duration.between(endDateTime, now).getSeconds();
            
            // 如果当前时间减去结束时间大于逾期阈值秒数，则认为逾期
            return diffSeconds > overdueThresholdSeconds;
        } catch (Exception e) {
            // 处理任何解析错误，默认不逾期
            return false;
        }
    }

    @Override
    public boolean checkUpcoming(int upcomingThreshold) {
        return false;
    }

    /**
     * 获取完整的开始时间
     * @return 完整的开始时间LocalDateTime，如果解析失败或time为null则返回null
     */
    @JsonIgnore
    public LocalDateTime getStartTime() {
        Time time = this.getTime();
        return time != null ? time.getStartTime(this.listId) : null;
    }
    
    /**
     * 获取完整的结束时间
     * @return 完整的结束时间LocalDateTime，如果解析失败或time为null则返回null
     */
    @JsonIgnore
    public LocalDateTime getEndTime() {
        Time time = this.getTime();
        return time != null ? time.getEndTime(this.listId) : null;
    }

    public LocalDate getDate() {
        // 使用 java.time 的 DateTimeFormatter 解析 listId：20251010 返回日期
        if (this.listId == null || this.listId.length() != 8) {
            return null;
        }
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            return java.time.LocalDate.parse(this.listId, formatter);
        } catch (Exception e) {
            return null;
        }
    }
        
    // 内部类：子任务
    public static class SubTodo implements CheckStatus {
        /**
         * 子任务文本内容
         */
        private String text;
        
        /**
         * 子任务完成状态
         */
        private Integer checked;
        
        /**
         * 子任务编辑状态
         */
        private Integer editing;

        // Getter and Setter methods for SubTodo class
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getChecked() {
            return checked;
        }

        public void setChecked(int checked) {
            this.checked = checked;
        }

        public boolean isEditing() {
            return this.editing != null && this.editing == 1;
        }

        public int getEditing() {
            return this.editing;
        }

        public void setEditing(int editing) {
            this.editing = editing;
        }

        @Override
        @JsonIgnore
        public boolean checkCompleted() {
            return this.checked == 1;
        }
        
        @Override
        @JsonIgnore
        public boolean checkOverdue(long overdueThresholdSeconds) {
            // 子任务没有时间信息，所以默认不逾期
            return false;
        }

        @Override
        public boolean checkUpcoming(int upcomingThreshold) {
            return false;
        }
    }

    // 内部类：时间
    public static class Time implements Comparable<Time> {
        /**
         * 开始时间 08: 00
         */
        private String start;
        
        /**
         * 结束时间 08: 00
         */
        private String end;

        // Getter and Setter methods for Time class
        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
        
        /**
         * 计算开始时间和结束时间之间的时间差，返回毫秒为单位的long值
         * @param listId 日期字符串，格式如20251225
         * @return 时间差（毫秒），如果时间格式不正确或开始时间晚于结束时间则返回0
         */
        public long calculateDurationMillis(String listId) {
            if (start == null || end == null) {
                return 0;
            }
            
            try {
                // 使用getStartTime和getEndTime方法获取完整的LocalDateTime对象
                java.time.LocalDateTime startTime = getStartTime(listId);
                java.time.LocalDateTime endTime = getEndTime(listId);
                
                // 检查解析是否成功
                if (startTime == null || endTime == null) {
                    return 0;
                }
                
                // 计算时间差（以毫秒为单位）
                java.time.Duration duration = java.time.Duration.between(startTime, endTime);
                long millis = duration.toMillis();
                
                // 如果开始时间晚于结束时间，返回0
                if (millis < 0) {
                    return 0;
                }
                
                return millis;
            } catch (Exception e) {
                // 处理时间格式错误等异常
                return 0;
            }
        }

        @Override
        public int compareTo(Time o) {
            // 如果this没有start时间，而o有start时间，this应该排在后面
            if (this.start == null && o.start != null) {
                return 1;
            }
            // 如果this有start时间，而o没有start时间，this应该排在前面
            if (this.start != null && o.start == null) {
                return -1;
            }
            // 如果两个都没有start时间，它们相等
            if (this.start == null && o.start == null) {
                return 0;
            }
            // 如果两个都有start时间，使用它们的start时间进行比较
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            java.time.LocalTime startTime = java.time.LocalTime.parse(this.start, formatter);
            java.time.LocalTime endTime = java.time.LocalTime.parse(o.start, formatter);
            return startTime.compareTo(endTime);
        }

        /**
         * 获取完整的开始时间
         * @param listId 日期字符串，格式如20251225
         * @return 完整的开始时间LocalDateTime，如果解析失败则返回null
         */
        public java.time.LocalDateTime getStartTime(String listId) {
            return parseDateTime(listId, this.start != null ? this.start : null);
        }
        
        /**
         * 获取完整的结束时间
         * @param listId 日期字符串，格式如20251225
         * @return 完整的结束时间LocalDateTime，如果解析失败则返回null
         */
        public java.time.LocalDateTime getEndTime(String listId) {
            return parseDateTime(listId, this.end != null ? this.end : null);
        }
        
        /**
         * 解析日期字符串和时间字符串为LocalDateTime
         * @param listId 日期字符串，格式如20251225
         * @param timeStr 时间字符串，格式如08:00
         * @return 解析后的LocalDateTime，如果解析失败则返回null
         */
        private java.time.LocalDateTime parseDateTime(String listId, String timeStr) {
            try {
                // 检查参数有效性
                if (listId == null || listId.length() != 8) {
                    return null;
                }
                if (timeStr == null || timeStr.trim().isEmpty()) {
                    return null;
                }
                
                // 解析日期部分
                int year = Integer.parseInt(listId.substring(0, 4));
                int month = Integer.parseInt(listId.substring(4, 6));
                int day = Integer.parseInt(listId.substring(6, 8));
                
                // 解析时间部分，先去除可能存在的空格
                java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                String cleanTimeStr = timeStr.replaceAll("\\s+", "");
                java.time.LocalTime time = java.time.LocalTime.parse(cleanTimeStr, timeFormatter);
                
                // 组合成完整的LocalDateTime
                return java.time.LocalDateTime.of(year, month, day, time.getHour(), time.getMinute());
            } catch (Exception e) {
                // 处理任何解析错误，返回null
                return null;
            }
        }
    }

    // Getter and Setter methods for Todo class
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SubTodo> getSubTodos() {
        return subTodos;
    }

    public void setSubTodos(List<SubTodo> subTodos) {
        this.subTodos = subTodos;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public String getRepeatingEventId() {
        return repeatingEventId;
    }

    public void setRepeatingEventId(String repeatingEventId) {
        this.repeatingEventId = repeatingEventId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
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

    @Override
    public int compareTo(Todo o) {
        // 如果this没有time字段，而o有time字段，this应该排在后面
        if (this.time == null && o.time != null) {
            return 1;
        }
        // 如果this有time字段，而o没有time字段，this应该排在前面
        if (this.time != null && o.time == null) {
            return -1;
        }
        // 如果两个都没有time字段，它们相等
        if (this.time == null && o.time == null) {
            return 0;
        }
        // 如果两个都有time字段，使用它们的time字段进行比较
        return this.time.compareTo(o.time);
    }
}
