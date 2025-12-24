package beordie.cn.service.impl;

import beordie.cn.service.TimeCacheStoreService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 时间缓存存储服务实现类，专门用于处理缓存操作
 */
@Service
public class TimeCacheStoreServiceImpl implements TimeCacheStoreService {

    /**
     * 获取todo的总消耗时间（毫秒）
     * @param todoId todo的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    @Override
    @Cacheable(value = "todoTimeCache", key = "'todo:' + #todoId", unless = "#result == null")
    public Long getTodoTotalTime(String todoId) {
        return 0L;
    }

    /**
     * 获取任务的总消耗时间（毫秒）
     * @param taskId 任务的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    @Override
    @Cacheable(value = "todoTimeCache", key = "'task:' + #taskId", unless = "#result == null")
    public Long getTaskTotalTime(String taskId) {
        return 0L;
    }

    /**
     * 获取里程碑的总消耗时间（毫秒）
     * @param milestoneId 里程碑的ID
     * @return 总消耗时间，如果没有记录则返回0
     */
    @Override
    @Cacheable(value = "todoTimeCache", key = "'milestone:' + #milestoneId", unless = "#result == null")
    public Long getMilestoneTotalTime(String milestoneId) {
        return 0L;
    }

    /**
     * 更新todo的总消耗时间
     * @param todoId todo的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    @Override
    @CachePut(value = "todoTimeCache", key = "'todo:' + #todoId")
    public Long updateTodoTotalTime(String todoId, long totalTime) {
        return totalTime;
    }

    /**
     * 更新任务的总消耗时间
     * @param taskId 任务的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    @Override
    @CachePut(value = "todoTimeCache", key = "'task:' + #taskId")
    public Long updateTaskTotalTime(String taskId, long totalTime) {
        return totalTime;
    }

    /**
     * 更新里程碑的总消耗时间
     * @param milestoneId 里程碑的ID
     * @param totalTime 总消耗时间
     * @return 更新后的总消耗时间
     */
    @Override
    @CachePut(value = "todoTimeCache", key = "'milestone:' + #milestoneId")
    public Long updateMilestoneTotalTime(String milestoneId, long totalTime) {
        return totalTime;
    }

    /**
     * 清除todo的时间缓存
     * @param todoId todo的ID
     */
    @Override
    @CacheEvict(value = "todoTimeCache", key = "'todo:' + #todoId")
    public void clearTodoTime(String todoId) {
        // 缓存清除操作由@CacheEvict注解处理
    }

    /**
     * 清除任务的时间缓存
     * @param taskId 任务的ID
     */
    @Override
    @CacheEvict(value = "todoTimeCache", key = "'task:' + #taskId")
    public void clearTaskTime(String taskId) {
        // 缓存清除操作由@CacheEvict注解处理
    }

    /**
     * 清除里程碑的时间缓存
     * @param milestoneId 里程碑的ID
     */
    @Override
    @CacheEvict(value = "todoTimeCache", key = "'milestone:' + #milestoneId")
    public void clearMilestoneTime(String milestoneId) {
        // 缓存清除操作由@CacheEvict注解处理
    }
}