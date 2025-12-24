package beordie.cn.repository;

import beordie.cn.model.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepository {
    Flux<Task> findAll();
    Flux<Task> findAll(String title, String category, String sortBy, String sortOrder);
    Mono<Task> findById(String id);
    Mono<Task> save(Task task);
    Mono<Task> update(String id, Task task);
    Mono<Void> deleteById(String id);
}
