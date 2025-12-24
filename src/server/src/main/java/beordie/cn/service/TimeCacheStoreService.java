package beordie.cn.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * 时间缓存存储服务接口，专门用于处理缓存操作
 */
public interface TimeCacheStoreService {
    /**
     * 获取todo的总消耗时间（毫秒）
     * @param todoId todo的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    Long getTodoTotalTime(String todoId);

    /**
     * 获取任务的总消耗时间（毫秒）
     * @param taskId 任务的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    Long getTaskTotalTime(String taskId);

    /**
     * 获取里程碑的总消耗时间（毫秒）
     * @param milestoneId 里程碑的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    Long getMilestoneTotalTime(String milestoneId);

    /**
     * 更新todo的总消耗时间
     * @param todoId todo的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    Long updateTodoTotalTime(String todoId, long totalTime);

    /**
     * 更新任务的总消耗时间
     * @param taskId 任务的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    Long updateTaskTotalTime(String taskId, long totalTime);

    /**
     * 更新里程碑的总消耗时间
     * @param milestoneId 里程碑的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    Long updateMilestoneTotalTime(String milestoneId, long totalTime);

    /**
     * 清除todo的时间缓存
     * @param todoId todo的ID
     */
    void clearTodoTime(String todoId);

    /**
     * 清除任务的时间缓存
     * @param taskId 任务的ID
     */
    void clearTaskTime(String taskId);

    /**
     * 清除里程碑的时间缓存
     * @param milestoneId 里程碑的ID
     */
    void clearMilestoneTime(String milestoneId);
}