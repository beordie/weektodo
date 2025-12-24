package beordie.cn.repository;

import beordie.cn.mapper.TodoMapper;
import beordie.cn.model.Todo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
public class MyBatisPlusTodoRepository implements TodoRepository {
    private final TodoMapper todoMapper;

    @Autowired
    public MyBatisPlusTodoRepository(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    @Override
    public Flux<Todo> findAll() {
        return Mono.fromSupplier(() -> todoMapper.selectList(null))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Todo> findById(String id) {
        return Mono.fromSupplier(() -> todoMapper.selectById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Todo> findByListId(String listId) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("list_id", listId);
            return todoMapper.selectList(queryWrapper);
        })
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Todo> findByTask(String task) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task", task);
            return todoMapper.selectList(queryWrapper);
        })
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Todo> findByMilestone(String milestone) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("milestone", milestone);
            return todoMapper.selectList(queryWrapper);
        })
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Todo> findByMilestoneId(String milestoneId) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("milestone_id", milestoneId);
            return todoMapper.selectList(queryWrapper);
        })
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Todo> save(Todo todo) {
        // 设置创建时间和更新时间
        if (todo.getCreatedAt() == null) {
            todo.setCreatedAt(java.time.LocalDateTime.now());
        }
        todo.setUpdatedAt(java.time.LocalDateTime.now());
        
        return Mono.fromCallable(() -> {
            todoMapper.insert(todo);
            return todo;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Todo> update(String id, Todo todo) {
        // 设置更新时间
        todo.setUpdatedAt(java.time.LocalDateTime.now());
        
        return Mono.fromCallable(() -> {
            todo.setId(id);
            todoMapper.updateById(todo);
            return todo;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> todoMapper.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> deleteByListId(String listId) {
        return Mono.fromRunnable(() -> {
            QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("list_id", listId);
            todoMapper.delete(queryWrapper);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
}
