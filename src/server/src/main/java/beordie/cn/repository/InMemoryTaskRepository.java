package beordie.cn.repository;

import beordie.cn.model.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
// @Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, Task> taskStore = new ConcurrentHashMap<>();

    @Override
    public Flux<Task> findAll() {
        return Flux.fromIterable(taskStore.values());
    }

    @Override
    public Flux<Task> findAll(String title, String category, String sortBy, String sortOrder) {
        // 基本查询和排序实现
        return Flux.fromIterable(taskStore.values())
                // 标题模糊查询
                .filter(task -> title == null || task.getTitle().toLowerCase().contains(title.toLowerCase()))
                // 分类精确查询
                .filter(task -> category == null || task.getCategory().equals(category))
                // 排序处理
                .sort((task1, task2) -> {
                    if (sortBy == null) {
                        return 0; // 默认不排序
                    }
                    int result = 0;
                    switch (sortBy) {
                        case "endDate":
                            result = compareNullSafe(task1.getEndDate(), task2.getEndDate());
                            break;
                        case "createdAt":
                            result = compareNullSafe(task1.getCreatedAt(), task2.getCreatedAt());
                            break;
                        case "priority":
                            result = compareNullSafe(task1.getPriority(), task2.getPriority());
                            break;
                        default:
                            result = 0;
                    }
                    // 处理排序方向
                    return "desc".equalsIgnoreCase(sortOrder) ? -result : result;
                });
    }

    // 辅助方法：安全比较可能为null的值
    private <T extends Comparable<T>> int compareNullSafe(T o1, T o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return o1.compareTo(o2);
    }

    @Override
    public Mono<Task> findById(String id) {
        Task task = taskStore.get(id);
        if (task != null) {
            return Mono.just(task);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<Task> save(Task task) {
        taskStore.put(task.getId(), task);
        return Mono.just(task);
    }

    @Override
    public Mono<Task> update(String id, Task updatedTask) {
        if (taskStore.containsKey(id)) {
            updatedTask.setId(id);
            taskStore.put(id, updatedTask);
            return Mono.just(updatedTask);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> deleteById(String id) {
        Task removedTask = taskStore.remove(id);
        if (removedTask != null) {
            return Mono.empty();
        } else {
            return Mono.error(new RuntimeException("Task not found with id: " + id));
        }
    }
}
