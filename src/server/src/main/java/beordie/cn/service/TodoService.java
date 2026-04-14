package beordie.cn.service;

import beordie.cn.model.Todo;
import beordie.cn.model.TodoSortKey;
import beordie.cn.notification.model.NotificationType;
import beordie.cn.repository.TodoRepository;
import beordie.cn.schedule.model.ScheduledTask;
import beordie.cn.schedule.service.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TodoService {
    private static final Logger log = LoggerFactory.getLogger(TodoService.class);
    
    private final TodoRepository todoRepository;
    private final TaskService taskService;
    private final MilestoneService milestoneService;
    private final TimeCacheService timeCacheService;
    private final RepeatingEventService repeatingEventService;
    private final ScheduledTaskService scheduledTaskService;

    @Autowired
    public TodoService(TodoRepository todoRepository, TaskService taskService, MilestoneService milestoneService, TimeCacheService timeCacheService, RepeatingEventService repeatingEventService, @Lazy ScheduledTaskService scheduledTaskService) {
        this.todoRepository = todoRepository;
        this.taskService = taskService;
        this.milestoneService = milestoneService;
        this.timeCacheService = timeCacheService;
        this.repeatingEventService = repeatingEventService;
        this.scheduledTaskService = scheduledTaskService;
    }

    // 辅助方法：根据taskId查询task title和color并赋值给Todo的task和color字段
    private Mono<Todo> setTaskTitleForTodo(Todo todo) {
        if (todo.getTaskId() != null && !todo.getTaskId().isEmpty()) {
            return taskService.getTaskById(todo.getTaskId())
                    .map(task -> {
                        todo.setTask(task.getTitle());
                        todo.setColor(task.getColor());
                        return todo;
                    })
                    .defaultIfEmpty(todo); // 如果task不存在，返回原始todo
        }
        return Mono.just(todo); // 如果没有taskId，直接返回原始todo
    }

    // 根据ID获取待办事项
    public Mono<Todo> getTodoById(String id) {
        return todoRepository.findById(id)
                .flatMap(this::setTaskTitleForTodo);
    }

     public Flux<Todo> getTodosByListIdDirectly(String listId) {
        return todoRepository.findByListId(listId);
     }

    // 根据列表ID获取待办事项
    public Flux<Todo> getTodosByListId(String listId) {
        return this.getTodosByListIdDirectly(listId)
                .flatMap(this::setTaskTitleForTodo)
                .filterWhen(todo -> {
                    if (todo.getRepeatingEventId() == null || todo.getRepeatingEventId().isBlank()) {
                        return Mono.just(true);
                    }
                    return repeatingEventService.getById(todo.getRepeatingEventId())
                            .hasElement()
                            .defaultIfEmpty(false);
                })
                .sort();
    }

    // 根据任务获取待办事项
    public Flux<Todo> getTodosByTask(String task) {
        return todoRepository.findByTask(task)
                .flatMap(this::setTaskTitleForTodo)
                .sort();
    }

    // 根据里程碑获取待办事项
    public Flux<Todo> getTodosByMilestone(String milestone) {
        return todoRepository.findByMilestone(milestone)
                .concatMap(this::setTaskTitleForTodo)
                .sort();
    }

    // 根据里程碑ID获取待办事项
    public Flux<Todo> getTodosByMilestoneId(String milestoneId) {
        return todoRepository.findByMilestoneId(milestoneId)
                .flatMap(this::setTaskTitleForTodo)
                .sort();
    }

    // 创建待办事项
    public Mono<Todo> createTodo(Todo todo) {
        // 检查 taskId 是否存在（如果不为空）
        Mono<Boolean> taskCheck = Mono.just(true);
        if (todo.getTaskId() != null && !todo.getTaskId().isEmpty()) {
            taskCheck = taskService.getTaskById(todo.getTaskId())
                    .hasElement()
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new IllegalArgumentException("Task ID does not exist: " + todo.getTaskId()));
                        }
                        return Mono.just(true);
                    });
        }

        // 检查 milestoneId 是否存在（如果不为空）
        Mono<Boolean> milestoneCheck = Mono.just(true);
        if (todo.getMilestoneId() != null && !todo.getMilestoneId().isEmpty()) {
            milestoneCheck = milestoneService.getMilestoneById(todo.getMilestoneId())
                    .hasElement()
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new IllegalArgumentException("Milestone ID does not exist: " + todo.getMilestoneId()));
                        }
                        return Mono.just(true);
                    });
        }

        // 设置创建时间和更新时间
        if (todo.getCreatedAt() == null) {
            todo.setCreatedAt(java.time.LocalDateTime.now());
        }
        todo.setUpdatedAt(java.time.LocalDateTime.now());

        // 执行所有检查并保存待办事项
        return Mono.zip(taskCheck, milestoneCheck)
                .flatMap(tuple -> todoRepository.save(todo))
                .flatMap(savedTodo -> {
                    // 在创建后处理todo状态变化和alarm
                    return timeCacheService.handleTodoStatusChange(savedTodo)
                            .doOnSuccess(v -> handleTodoAlarm(savedTodo))
                            .thenReturn(savedTodo);
                });
    }

    // 更新待办事项
    public Mono<Todo> updateTodo(String id, Todo todo) {
        // 检查 taskId 是否存在（如果不为空）
        Mono<Boolean> taskCheck = Mono.just(true);
        if (todo.getTaskId() != null && !todo.getTaskId().isEmpty()) {
            taskCheck = taskService.getTaskById(todo.getTaskId())
                    .hasElement()
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new IllegalArgumentException("Task ID does not exist: " + todo.getTaskId()));
                        }
                        return Mono.just(true);
                    });
        }

        // 检查 milestoneId 是否存在（如果不为空）
        Mono<Boolean> milestoneCheck = Mono.just(true);
        if (todo.getMilestoneId() != null && !todo.getMilestoneId().isEmpty()) {
            milestoneCheck = milestoneService.getMilestoneById(todo.getMilestoneId())
                    .hasElement()
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new IllegalArgumentException("Milestone ID does not exist: " + todo.getMilestoneId()));
                        }
                        return Mono.just(true);
                    });
        }

        // 设置更新时间
        todo.setUpdatedAt(java.time.LocalDateTime.now());
        // 如果重复事件ID为空字符串或仅包含空白，置为null以清空数据库字段
        if (todo.getRepeatingEventId() != null && todo.getRepeatingEventId().trim().isEmpty()) {
            todo.setRepeatingEventId(null);
        }

        // 执行所有检查并更新待办事项
        return Mono.zip(taskCheck, milestoneCheck)
                .flatMap(tuple -> todoRepository.update(id, todo))
                .flatMap(updatedTodo -> {
                    // 在更新后处理todo状态变化和alarm
                    return timeCacheService.handleTodoStatusChange(updatedTodo)
                            .doOnSuccess(v -> handleTodoAlarm(updatedTodo))
                            .thenReturn(updatedTodo);
                })
                .flatMap(this::setTaskTitleForTodo); // 设置task字段
    }

    // 删除待办事项
    public Mono<Void> deleteTodo(String id) {
        return todoRepository.findById(id)
                .flatMap(todo -> {
                    Mono<Void> deleteRepeat = Mono.empty();
                    if (todo.getRepeatingEventId() != null && !todo.getRepeatingEventId().isEmpty()) {
                        deleteRepeat = repeatingEventService.deleteById(todo.getRepeatingEventId());
                    }
                    if (todo.getAlarm() == 1) {
                        scheduledTaskService.cancelTaskByTodoId(id);
                    }
                    return deleteRepeat.then(todoRepository.deleteById(id));
                })
                .switchIfEmpty(todoRepository.deleteById(id));
    }

    // 根据列表ID删除待办事项
    public Mono<Void> deleteTodosByListId(String listId) {
        return todoRepository.deleteByListId(listId);
    }

    // 切换待办事项的完成状态
    public Mono<Todo> toggleTodo(String id) {
        return todoRepository.findById(id)
                .flatMap(todo -> {
                    todo.setChecked(todo.checkCompleted() ? 0 : 1);
                    todo.setUpdatedAt(java.time.LocalDateTime.now());
                    return todoRepository.update(id, todo);
                })
                .flatMap(updatedTodo -> {
                    // 在状态切换后处理todo状态变化
                    return timeCacheService.handleTodoStatusChange(updatedTodo)
                            .thenReturn(updatedTodo);
                })
                .flatMap(this::setTaskTitleForTodo); // 设置task字段
    }

    // 切换子任务的完成状态
    public Mono<Todo> toggleSubTask(String id, int index) {
        return todoRepository.findById(id)
                .flatMap(todo -> {
                    if (index >= 0 && index < todo.getSubTodos().size()) {
                        Todo.SubTodo subTask = todo.getSubTodos().get(index);
                        subTask.setChecked(subTask.checkCompleted() ? 0 : 1);
                        todo.setUpdatedAt(java.time.LocalDateTime.now());
                        return todoRepository.update(id, todo);
                    }
                    return Mono.error(new IllegalArgumentException("Invalid sub-task index"));
                })
                .flatMap(updatedTodo -> {
                    // 在子任务状态切换后处理todo状态变化
                    return timeCacheService.handleTodoStatusChange(updatedTodo)
                            .thenReturn(updatedTodo);
                });
    }
    
    public Flux<Todo> getTodos(beordie.cn.web.TodoPageQuery q) {
        TodoSortKey sortKey = TodoSortKey.from(q.getSortBy());
        return todoRepository.findByTaskIdPagedSorted(q.getTaskId(), q.offset(), q.limit(), sortKey, q.desc());
    }
    
    // 获取所有待办事项
    public Flux<Todo> getAllTodos() {
        return todoRepository.findAll()
                .flatMap(this::setTaskTitleForTodo);
    }
    
    private void handleTodoAlarm(Todo todo) {
        if (todo.getId() == null) {
            return;
        }
        
        log.info("Handling todo alarm: todoId={}, alarm={}", todo.getId(), todo.getAlarm());
        
        scheduledTaskService.cancelTaskByTodoId(todo.getId());
        
        if (todo.getAlarm() == 1) {
            createAlarmTask(todo);
        }
    }
    
    private void createAlarmTask(Todo todo) {
        if (todo.getTime() == null) {
            log.warn("Todo has no time, cannot create alarm task: todoId={}", todo.getId());
            return;
        }
        
        java.time.LocalDateTime executeTime = todo.getStartTime();
        if (executeTime == null) {
            executeTime = todo.getEndTime();
        }
        
        if (executeTime == null) {
            log.warn("Todo has no valid start/end time, cannot create alarm task: todoId={}", todo.getId());
            return;
        }
        
        if (executeTime.isBefore(java.time.LocalDateTime.now())) {
            log.warn("Todo time is in past, skipping alarm task: todoId={}, time={}", todo.getId(), executeTime);
            return;
        }
        
        ScheduledTask task = new ScheduledTask();
        task.setTodoId(todo.getId());
        task.setName("Todo Alarm: " + todo.getText());
        task.setDescription("Alarm for todo: " + todo.getId());
        task.setExecuteTime(executeTime);
        task.setNotificationType(NotificationType.FEISHU);
        task.setNotificationTitle("待办事项提醒");
        task.setNotificationContent("您有待办事项需要处理：" + todo.getText());
        
        if (todo.getTime() != null) {
            if (todo.getTime().getStart() != null) {
                java.time.LocalDateTime startTime = todo.getStartTime();
                if (startTime != null) {
                    task.setNotificationStartTime(startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
            if (todo.getTime().getEnd() != null) {
                java.time.LocalDateTime endTime = todo.getEndTime();
                if (endTime != null) {
                    task.setNotificationEndTime(endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        }
        
        log.info("Creating alarm task for todo: todoId={}, executeTime={}", todo.getId(), executeTime);
        scheduledTaskService.createTask(task);
    }
}
