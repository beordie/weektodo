package beordie.cn.dashboard;

import beordie.cn.handler.ConfigHandler.TaskTimeConfig;
import beordie.cn.model.Todo;
import beordie.cn.service.TimeCacheService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class TaskDashboardContext extends DashboardContext {
    private final List<Todo> todos;

    public TaskDashboardContext(List<Todo> todos, TaskTimeConfig taskTimeConfig, TimeCacheService timeCacheService) {
        super(Collections.emptyList(), LocalDateTime.now(), taskTimeConfig, timeCacheService);
        this.todos = todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }
}