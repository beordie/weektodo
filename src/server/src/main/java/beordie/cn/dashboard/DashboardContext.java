package beordie.cn.dashboard;

import beordie.cn.handler.ConfigHandler.TaskTimeConfig;
import beordie.cn.model.Task;
import beordie.cn.service.TimeCacheService;
import beordie.cn.service.TimeCacheStoreService;
import beordie.cn.service.impl.TimeCacheServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardContext {
    private final List<Task> tasks;
    private final LocalDateTime now;
    private final TaskTimeConfig timeConfig;
    private final TimeCacheService timeCacheService;

    public DashboardContext(List<Task> tasks, LocalDateTime now, TaskTimeConfig timeConfig, TimeCacheService timeCacheService) {
        this.tasks = tasks;
        this.now = now;
        this.timeConfig = timeConfig;
        this.timeCacheService = timeCacheService;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public LocalDateTime getNow() {
        return now;
    }

    public TaskTimeConfig getTimeConfig() {
        return timeConfig;
    }

    public TimeCacheService getTimeCacheService() {
        return timeCacheService;
    }
}

