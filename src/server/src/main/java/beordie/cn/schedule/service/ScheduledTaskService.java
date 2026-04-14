package beordie.cn.schedule.service;

import beordie.cn.schedule.executor.ScheduledTaskExecutor;
import beordie.cn.schedule.model.ScheduledTask;
import beordie.cn.schedule.model.ScheduledTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduledTaskService {
    
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);
    
    private final TaskScheduler taskScheduler;
    private final ScheduledTaskExecutor taskExecutor;
    
    private final Map<String, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    
    public ScheduledTaskService(TaskScheduler taskScheduler, @Lazy ScheduledTaskExecutor taskExecutor) {
        this.taskScheduler = taskScheduler;
        this.taskExecutor = taskExecutor;
    }
    
    public void createTask(ScheduledTask task) {
        log.info("Creating scheduled task: name={}", task.getName());
        tasks.put(task.getId(), task);
        scheduleTask(task);
    }
    
    public void updateTask(ScheduledTask task) {
        log.info("Updating scheduled task: id={}", task.getId());
        tasks.put(task.getId(), task);
    }
    
    public void cancelTask(String id) {
        log.info("Cancelling scheduled task: id={}", id);
        ScheduledFuture<?> future = futures.get(id);
        if (future != null) {
            future.cancel(false);
            futures.remove(id);
        }
        
        ScheduledTask task = tasks.get(id);
        if (task != null) {
            task.setStatus(ScheduledTaskStatus.CANCELLED);
        }
    }
    
    public void cancelTaskByTodoId(String todoId) {
        log.info("Cancelling tasks for todoId: {}", todoId);
        List<String> taskIdsToCancel = tasks.values().stream()
                .filter(task -> todoId.equals(task.getTodoId()))
                .map(ScheduledTask::getId)
                .toList();
        
        for (String taskId : taskIdsToCancel) {
            cancelTask(taskId);
        }
    }
    
    private void scheduleTask(ScheduledTask task) {
        if (task.getCronExpression() != null && !task.getCronExpression().isBlank()) {
            scheduleCronTask(task);
        } else if (task.getExecuteTime() != null) {
            scheduleOneTimeTask(task);
        } else {
            log.warn("Task has no schedule: id={}", task.getId());
        }
    }
    
    private void scheduleCronTask(ScheduledTask task) {
        log.info("Scheduling cron task: id={}, cron={}", task.getId(), task.getCronExpression());
        
        Runnable taskRunnable = () -> {
            log.info("Cron task triggered: id={}", task.getId());
            taskExecutor.executeTask(task).subscribe();
        };
        
        CronTrigger trigger = new CronTrigger(task.getCronExpression());
        ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, trigger);
        futures.put(task.getId(), future);
    }
    
    private void scheduleOneTimeTask(ScheduledTask task) {
        log.info("Scheduling one-time task: id={}, time={}", task.getId(), task.getExecuteTime());
        
        Runnable taskRunnable = () -> {
            log.info("One-time task triggered: id={}", task.getId());
            taskExecutor.executeTask(task).subscribe();
            futures.remove(task.getId());
        };
        
        Date startTime = Date.from(task.getExecuteTime()
                .atZone(ZoneId.systemDefault())
                .toInstant());
        
        ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, startTime);
        futures.put(task.getId(), future);
    }
}
