package beordie.cn.repository;

import beordie.cn.mapper.TaskMapper;
import beordie.cn.model.Task;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
public class MyBatisPlusTaskRepository implements TaskRepository {
    private final TaskMapper taskMapper;

    @Autowired
    public MyBatisPlusTaskRepository(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public Flux<Task> findAll() {
        return findAll(null, null, null, null);
    }

    @Override
    public Flux<Task> findAll(String title, String category, String sortBy, String sortOrder) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
            
            // 添加标题模糊查询条件
            if (title != null && !title.isEmpty()) {
                queryWrapper.like("title", title);
            }
            
            // 添加分类精确查询条件
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("category", category);
            }
            
            // 添加排序条件
            if (sortBy != null && !sortBy.isEmpty()) {
                // 验证排序字段是否合法并转换为数据库字段名
                String dbFieldName;
                boolean isValidSortField = "title".equals(sortBy) || "category".equals(sortBy) || 
                                          "endDate".equals(sortBy) || "createdAt".equals(sortBy) || 
                                          "priority".equals(sortBy) || "updatedAt".equals(sortBy);
                
                if (isValidSortField) {
                    // 转换为数据库下划线命名方式
                    dbFieldName = switch (sortBy) {
                        case "endDate" -> "end_date";
                        case "createdAt" -> "created_at";
                        case "updatedAt" -> "updated_at";
                        default -> sortBy; // title, category, priority 与数据库字段名一致
                    };
                    
                    String order = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
                    queryWrapper.orderBy(true, "asc".equals(order), dbFieldName);
                }
            }
            
            return taskMapper.selectList(queryWrapper);
        })
        .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Task> findById(String id) {
        return Mono.fromSupplier(() -> taskMapper.selectById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Task> save(Task task) {
        return Mono.fromCallable(() -> {
            taskMapper.insert(task);
            return task;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Task> update(String id, Task task) {
        return Mono.fromCallable(() -> {
            task.setId(id);
            taskMapper.updateById(task);
            return task;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> taskMapper.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
