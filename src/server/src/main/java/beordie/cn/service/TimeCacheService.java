package beordie.cn.service;

import beordie.cn.model.Todo;
import reactor.core.publisher.Mono;

/**
 * 时间缓存服务接口，用于管理todo消耗时间的缓存
 */
public interface TimeCacheService {
    /**
     * 获取任务的总消耗时间（毫秒）
     * @param taskId 任务的ID
     * @return 返回总消耗的毫秒数，如果没有记录则返回0
     */
    long getTaskTotalTime(String taskId);

    /**
     * 获取任务的总消耗时间（小时）
     * @param taskId 任务的ID
     * @return Mono<Double> 返回总消耗的小时数，如果没有记录则返回0
     */
    Mono<Double> getTaskTotalTimeInHours(String taskId);

    /**
     * 获取里程碑的总消耗时间（毫秒）
     * @param milestoneId 里程碑的ID
     * @return 返回总消耗的毫秒数，如果没有记录则返回0
     */
    long getMilestoneTotalTime(String milestoneId);

    /**
     * 增加todo的消耗时间
     * @param todoId todo的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    Mono<Void> addTime(String todoId, long milliseconds);

    /**
     * 增加任务的消耗时间
     * @param taskId 任务的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    Mono<Void> addTaskTime(String taskId, long milliseconds);

    /**
     * 增加里程碑的消耗时间
     * @param milestoneId 里程碑的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    Mono<Void> addMilestoneTime(String milestoneId, long milliseconds);

    /**
     * 清除todo的时间缓存
     * @param todoId todo的ID
     * @return Mono<Void>
     */
    Mono<Void> clearTime(String todoId);

    /**
     * 清除任务的时间缓存
     * @param taskId 任务的ID
     * @return Mono<Void>
     */
    Mono<Void> clearTaskTime(String taskId);

    /**
     * 清除里程碑的时间缓存
     * @param milestoneId 里程碑的ID
     * @return Mono<Void>
     */
    Mono<Void> clearMilestoneTime(String milestoneId);
    
    /**
     * 根据todo的状态执行缓存操作
     * @param todo 待办事项对象
     * @return Mono<Void>
     */
    Mono<Void> handleTodoStatusChange(Todo todo);
}