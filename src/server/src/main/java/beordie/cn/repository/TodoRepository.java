package beordie.cn.repository;

import beordie.cn.model.Todo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TodoRepository {
    // 获取所有待办事项
    Flux<Todo> findAll();
    
    // 根据ID获取待办事项
    Mono<Todo> findById(String id);
    
    // 根据列表ID获取待办事项
    Flux<Todo> findByListId(String listId);
    
    // 根据任务获取待办事项
    Flux<Todo> findByTask(String task);
    
    // 根据里程碑获取待办事项
    Flux<Todo> findByMilestone(String milestone);
    
    // 根据里程碑ID获取待办事项
    Flux<Todo> findByMilestoneId(String milestoneId);
    
    // 根据任务ID获取待办事项
    Flux<Todo> findByTaskId(String taskId);
    
    // 保存待办事项
    Mono<Todo> save(Todo todo);
    
    // 更新待办事项
    Mono<Todo> update(String id, Todo todo);
    
    // 删除待办事项
    Mono<Void> deleteById(String id);
    
    // 根据列表ID删除待办事项
    Mono<Void> deleteByListId(String listId);
    
    // 根据任务ID删除待办事项
    Mono<Void> deleteByTaskId(String taskId);
    
    // 根据任务ID和listId范围查询待办事项并按listId分组统计数量
    Flux<java.util.Map.Entry<String, Integer>> findByTaskIdAndListIdGreaterThanGroupByListId(String taskId, String listId);
    
    // 根据任务ID分页查询待办事项，支持排序（参数已在服务层归一化）
    Flux<Todo> findByTaskIdPagedSorted(String taskId, int offset, int limit, beordie.cn.model.TodoSortKey sortKey, boolean desc);
}
