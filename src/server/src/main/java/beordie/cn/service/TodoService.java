package beordie.cn.service;

import beordie.cn.model.Todo;
import beordie.cn.model.TodoSortKey;
import beordie.cn.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final TaskService taskService;
    private final MilestoneService milestoneService;
    private final TimeCacheService timeCacheService;

    @Autowired
    public TodoService(TodoRepository todoRepository, TaskService taskService, MilestoneService milestoneService, TimeCacheService timeCacheService) {
        this.todoRepository = todoRepository;
        this.taskService = taskService;
        this.milestoneService = milestoneService;
        this.timeCacheService = timeCacheService;
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

    // 根据列表ID获取待办事项
    public Flux<Todo> getTodosByListId(String listId) {
        return todoRepository.findByListId(listId)
                .flatMap(this::setTaskTitleForTodo)
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
                    // 在创建后处理todo状态变化
                    return timeCacheService.handleTodoStatusChange(savedTodo)
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
                    // 在更新后处理todo状态变化
                    return timeCacheService.handleTodoStatusChange(updatedTodo)
                            .thenReturn(updatedTodo);
                })
                .flatMap(this::setTaskTitleForTodo); // 设置task字段
    }

    // 删除待办事项
    public Mono<Void> deleteTodo(String id) {
        return todoRepository.deleteById(id);
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
}
