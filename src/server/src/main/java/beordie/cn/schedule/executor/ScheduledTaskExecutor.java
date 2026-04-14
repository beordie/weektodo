package beordie.cn.schedule.executor;

import beordie.cn.notification.model.Notification;
import beordie.cn.notification.model.NotificationResult;
import beordie.cn.notification.service.NotificationService;
import beordie.cn.schedule.model.ScheduledTask;
import beordie.cn.schedule.model.ScheduledTaskStatus;
import beordie.cn.schedule.service.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class ScheduledTaskExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskExecutor.class);
    
    private final NotificationService notificationService;
    private final ScheduledTaskService scheduledTaskService;
    
    public ScheduledTaskExecutor(NotificationService notificationService, ScheduledTaskService scheduledTaskService) {
        this.notificationService = notificationService;
        this.scheduledTaskService = scheduledTaskService;
    }
    
    public Mono<NotificationResult> executeTask(ScheduledTask task) {
        log.info("Executing scheduled task: id={}, name={}", task.getId(), task.getName());
        
        task.setStatus(ScheduledTaskStatus.RUNNING);
        scheduledTaskService.updateTask(task);
        
        try {
            Notification notification = buildNotification(task);
            Mono<NotificationResult> resultMono = notificationService.send(notification);
            
            return resultMono.doOnSuccess(result -> {
                if (result.isSuccess()) {
                    task.setStatus(ScheduledTaskStatus.COMPLETED);
                    log.info("Scheduled task completed successfully: id={}", task.getId());
                } else {
                    task.setStatus(ScheduledTaskStatus.FAILED);
                    task.setErrorMessage(result.getMessage());
                    log.error("Scheduled task failed: id={}, error={}", task.getId(), result.getMessage());
                }
                task.setExecutedAt(LocalDateTime.now());
                scheduledTaskService.updateTask(task);
            }).doOnError(ex -> {
                task.setStatus(ScheduledTaskStatus.FAILED);
                task.setErrorMessage(ex.getMessage());
                task.setExecutedAt(LocalDateTime.now());
                scheduledTaskService.updateTask(task);
                log.error("Scheduled task execution error: id={}", task.getId(), ex);
            });
        } catch (Exception ex) {
            task.setStatus(ScheduledTaskStatus.FAILED);
            task.setErrorMessage(ex.getMessage());
            task.setExecutedAt(LocalDateTime.now());
            scheduledTaskService.updateTask(task);
            log.error("Scheduled task execution error: id={}", task.getId(), ex);
            return Mono.just(NotificationResult.failure(ex.getMessage()));
        }
    }
    
    private Notification buildNotification(ScheduledTask task) {
        Notification notification = new Notification();
        notification.setTitle(task.getNotificationTitle());
        notification.setContent(task.getNotificationContent());
        notification.setRecipient(task.getNotificationRecipient());
        notification.setType(task.getNotificationType());
        notification.setStartTime(task.getNotificationStartTime());
        notification.setEndTime(task.getNotificationEndTime());
        if (task.getExtra() != null) {
            notification.setExtra(task.getExtra());
        }
        return notification;
    }
}
