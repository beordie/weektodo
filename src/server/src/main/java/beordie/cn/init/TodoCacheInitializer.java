package beordie.cn.init;

import beordie.cn.model.Todo;
import beordie.cn.service.TodoService;
import beordie.cn.service.TimeCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * 项目启动时执行的初始化类，用于从数据库拉取所有todo列表并更新缓存
 */
@Component
public class TodoCacheInitializer implements CommandLineRunner {
    private final TodoService todoService;
    private final TimeCacheService timeCacheService;

    @Autowired
    public TodoCacheInitializer(TodoService todoService, TimeCacheService timeCacheService) {
        this.todoService = todoService;
        this.timeCacheService = timeCacheService;
    }

    @Override
    public void run(String... args) {
        // 创建一个Set用于存储所有唯一的taskId
        java.util.Set<String> taskIdSet = new java.util.HashSet<>();
        
        // 在项目启动时，异步从数据库获取所有todo列表
        todoService.getAllTodos()
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        todo -> {
                            // 将todo关联的taskId添加到Set中
                            if (todo.getTaskId() != null) {
                                taskIdSet.add(todo.getTaskId());
                            }
                            // 处理每个todo，更新缓存
                            timeCacheService.handleTodoStatusChange(todo)
                                    .subscribe(
                                            success -> System.out.println("Todo缓存更新成功: " + todo.getId()),
                                            error -> System.err.println("Todo缓存更新失败: " + todo.getId() + ", 错误: " + error.getMessage())
                                    );
                        },
                        error -> System.err.println("获取所有todo失败: " + error.getMessage()),
                        () -> {
                            System.out.println("所有todo缓存更新完成");
                            // 在所有todo处理完成后，遍历Set中的taskId，查看每个任务的总时间
                            for (String taskId : taskIdSet) {
                                timeCacheService.getTaskTotalTimeInHours(taskId)
                                        .subscribe(
                                                totalTime -> {
                                                    System.out.println("任务ID " + taskId + ": 总时间 " + totalTime + "小时");
                                                },
                                                error -> System.err.println("获取任务 " + taskId + " 的总时间失败: " + error.getMessage())
                                        );
                            }
                        }
                );
    }
}
