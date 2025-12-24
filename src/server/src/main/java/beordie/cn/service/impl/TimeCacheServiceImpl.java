package beordie.cn.service.impl;

import beordie.cn.model.Todo;
import beordie.cn.repository.TodoRepository;
import beordie.cn.service.TimeCacheService;
import beordie.cn.service.TimeCacheStoreService;
import beordie.cn.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * 时间缓存服务实现类，使用Spring Cache管理todo消耗时间
 */
@Service
public class TimeCacheServiceImpl implements TimeCacheService {
    private final TimeCacheStoreService timeCacheStoreService;

    @Autowired
    public TimeCacheServiceImpl(TimeCacheStoreService timeCacheStoreService) {
        this.timeCacheStoreService = timeCacheStoreService;
    }
    
    /**
     * 获取任务的总消耗时间（毫秒）
     * @param taskId 任务的ID
     * @return 返回总消耗的毫秒数，如果没有记录则返回0
     */
    @Override
    public long getTaskTotalTime(String taskId) {
        return timeCacheStoreService.getTaskTotalTime(taskId);
    }

    /**
     * 获取里程碑的总消耗时间（毫秒）
     * @param milestoneId 里程碑的ID
     * @return 返回总消耗的毫秒数，如果没有记录则返回0
     */
    @Override
    public long getMilestoneTotalTime(String milestoneId) {
        return timeCacheStoreService.getMilestoneTotalTime(milestoneId);
    }

    /**
     * 获取任务的总消耗时间（小时）
     * @param taskId 任务的ID
     * @return Mono<Double> 返回总消耗的小时数，如果没有记录则返回0
     */
    @Override
    public Mono<Double> getTaskTotalTimeInHours(String taskId) {
        // 转换毫秒为小时（1小时 = 3600000毫秒）
        long totalMilliseconds = getTaskTotalTime(taskId);
        double totalHours = totalMilliseconds / 3600000.0;
        return Mono.just(totalHours);
    }

    /**
     * 增加todo的消耗时间
     * @param todoId todo的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> addTime(String todoId, long milliseconds) {
        return Mono.fromRunnable(() -> {
            addTimeSync(todoId, milliseconds, null, null);
        });
    }

    /**
     * 增加任务的消耗时间
     * @param taskId 任务的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> addTaskTime(String taskId, long milliseconds) {
        return Mono.fromRunnable(() -> {
            addTimeSync(null, milliseconds, taskId, null);
        });
    }

    /**
     * 增加里程碑的消耗时间
     * @param milestoneId 里程碑的ID
     * @param milliseconds 要增加的毫秒数
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> addMilestoneTime(String milestoneId, long milliseconds) {
        return Mono.fromRunnable(() -> {
            addTimeSync(null, milliseconds, null, milestoneId);
        });
    }



    /**
     * 同步增加时间
     * @param todoId todo的ID（可选）
     * @param milliseconds 要增加的毫秒数
     * @param taskId 任务的ID（可选）
     * @param milestoneId 里程碑的ID（可选）
     */
    private void addTimeSync(String todoId, long milliseconds, String taskId, String milestoneId) {
        // 增加任务的时间
        if (taskId != null) {
            Long currentTaskTime = timeCacheStoreService.getTaskTotalTime(taskId);
            long newTaskTotalTime = currentTaskTime + milliseconds;
            timeCacheStoreService.updateTaskTotalTime(taskId, newTaskTotalTime);
        }
        
        // 增加里程碑的时间
        if (milestoneId != null) {
            Long currentMilestoneTime = timeCacheStoreService.getMilestoneTotalTime(milestoneId);
            long newMilestoneTotalTime = currentMilestoneTime + milliseconds;
            timeCacheStoreService.updateMilestoneTotalTime(milestoneId, newMilestoneTotalTime);
        }
    }

    /**
     * 清除todo的时间缓存
     * @param todoId todo的ID
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> clearTime(String todoId) {
        return Mono.fromRunnable(() -> timeCacheStoreService.clearTodoTime(todoId));
    }

    /**
     * 清除任务的时间缓存
     * @param taskId 任务的ID
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> clearTaskTime(String taskId) {
        return Mono.fromRunnable(() -> timeCacheStoreService.clearTaskTime(taskId));
    }

    /**
     * 清除里程碑的时间缓存
     * @param milestoneId 里程碑的ID
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> clearMilestoneTime(String milestoneId) {
        return Mono.fromRunnable(() -> timeCacheStoreService.clearMilestoneTime(milestoneId));
    }
    
    /**
     * 根据todo的状态执行缓存操作
     * @param todo 待办事项对象
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> handleTodoStatusChange(Todo todo) {
        String todoId = todo.getId();
        String taskId = todo.getTaskId();
        String milestoneId = todo.getMilestoneId();
        
        // 根据todo的状态执行不同的操作
        if (todo.checkCompleted()) {
            // 任务已完成，只使用todo对象中的time属性计算时间
            return Mono.fromRunnable(() -> {
                // 如果todo有时间安排，计算总时间并添加到缓存
                if (todo.getTime() != null) {
                    // 使用Time类自身的calculateDurationMillis方法计算时间差（毫秒）
                    long timeDuration = todo.getTime().calculateDurationMillis();
                    if (timeDuration > 0) {
                        addTimeSync(todoId, timeDuration, taskId, milestoneId);
                    }
                }
            });
        } else {
            // 任务未完成，只清除时间缓存
            return Mono.when(
                clearTime(todoId),
                taskId != null ? clearTaskTime(taskId) : Mono.empty(),
                milestoneId != null ? clearMilestoneTime(milestoneId) : Mono.empty()
            );
        }
    }
    

}