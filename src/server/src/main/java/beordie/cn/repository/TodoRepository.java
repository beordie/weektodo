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
    
    // 保存待办事项
    Mono<Todo> save(Todo todo);
    
    // 更新待办事项
    Mono<Todo> update(String id, Todo todo);
    
    // 删除待办事项
    Mono<Void> deleteById(String id);
    
    // 根据列表ID删除待办事项
    Mono<Void> deleteByListId(String listId);
}
