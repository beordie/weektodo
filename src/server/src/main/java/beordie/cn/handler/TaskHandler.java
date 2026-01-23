package beordie.cn.handler;

import beordie.cn.dashboard.milestone.Statistics;
import beordie.cn.dashboard.dto.DashboardResponse;
import beordie.cn.model.Milestone;
import beordie.cn.model.Task;
import beordie.cn.service.MilestoneService;
import beordie.cn.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TaskHandler {

    private final TaskService taskService;
    private final MilestoneService milestoneService;

    @Autowired
    public TaskHandler(TaskService taskService, MilestoneService milestoneService) {
        this.taskService = taskService;
        this.milestoneService = milestoneService;
    }

    // 获取所有任务，支持查询和排序
    public Mono<ServerResponse> getAllTasks(ServerRequest request) {
        // 从请求参数中提取查询条件
        String title = request.queryParam("title").orElse(null);
        String category = request.queryParam("category").orElse(null);
        String sortBy = request.queryParam("sortBy").orElse(null);
        String sortOrder = request.queryParam("sortOrder").orElse(null);

        // 调用服务获取任务
        Flux<Task> tasks = taskService.getAllTasks(title, category, sortBy, sortOrder);

        // 返回响应
        return ServerResponse.ok()
                .body(tasks, Task.class);
    }

    // 根据ID获取任务
    public Mono<ServerResponse> getTaskById(ServerRequest request) {
        String id = request.pathVariable("id");

        return taskService.getTaskById(id)
                .flatMap(task -> ServerResponse.ok().body(Mono.just(task), Task.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 创建任务
    public Mono<ServerResponse> createTask(ServerRequest request) {
        Mono<Task> taskMono = request.bodyToMono(Task.class);

        return taskMono
                .flatMap(taskService::createTask)
                .flatMap(task -> ServerResponse.status(HttpStatus.CREATED)
                        .body(Mono.just(task), Task.class));
    }

    // 更新任务
    public Mono<ServerResponse> updateTask(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Task> taskMono = request.bodyToMono(Task.class);

        return taskMono
                .flatMap(task -> taskService.updateTask(id, task))
                .flatMap(task -> ServerResponse.ok().body(Mono.just(task), Task.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 删除任务
    public Mono<ServerResponse> deleteTask(ServerRequest request) {
        String id = request.pathVariable("id");

        return taskService.deleteTask(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(e -> ServerResponse.notFound().build());
    }

    // 获取任务的所有里程碑
    public Mono<ServerResponse> getMilestonesByTaskId(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        Flux<Milestone> milestones = milestoneService.getMilestonesByTaskId(taskId);
        return ServerResponse.ok()
                .body(milestones, Milestone.class);
    }

    // 获取任务的特定里程碑
    public Mono<ServerResponse> getMilestoneByTaskIdAndId(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        String milestoneId = request.pathVariable("milestoneId");

        return milestoneService.getMilestoneByTaskIdAndId(taskId, milestoneId)
                .flatMap(milestone -> ServerResponse.ok().body(Mono.just(milestone), Milestone.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 为任务创建里程碑
    public Mono<ServerResponse> createMilestone(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        Mono<Milestone> milestoneMono = request.bodyToMono(Milestone.class);

        return milestoneMono
                .flatMap(milestone -> {
                    // 设置里程碑的任务ID
                    milestone.setTaskId(taskId);
                    return milestoneService.createMilestone(milestone);
                })
                .flatMap(milestone -> ServerResponse.status(HttpStatus.CREATED)
                        .body(Mono.just(milestone), Milestone.class));
    }

    // 更新任务的里程碑
    public Mono<ServerResponse> updateMilestone(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        String milestoneId = request.pathVariable("milestoneId");
        Mono<Milestone> milestoneMono = request.bodyToMono(Milestone.class);

        return milestoneService.getMilestoneByTaskIdAndId(taskId, milestoneId)
                .flatMap(existingMilestone -> milestoneMono)
                .flatMap(milestone -> {
                    // 确保里程碑的任务ID正确
                    milestone.setTaskId(taskId);
                    return milestoneService.updateMilestone(milestoneId, milestone);
                })
                .flatMap(milestone -> ServerResponse.ok().body(Mono.just(milestone), Milestone.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 删除任务的里程碑
    public Mono<ServerResponse> deleteMilestone(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        String milestoneId = request.pathVariable("milestoneId");

        return milestoneService.getMilestoneByTaskIdAndId(taskId, milestoneId)
                .flatMap(milestone -> milestoneService.deleteMilestone(milestoneId))
                .then(ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 获取任务整体大盘数据
    public Mono<ServerResponse> getTaskDashboardData(ServerRequest request) {
        return taskService.getTaskDashboardData()
                .flatMap(dashboardData -> ServerResponse.ok()
                        .body(Mono.just(dashboardData), DashboardResponse.class));
    }

    // 根据任务ID获取任务看板数据
    public Mono<ServerResponse> getTaskDashboardDataByTaskId(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        return taskService.getTaskDashboardDataByTaskId(taskId)
                .flatMap(dashboardData -> ServerResponse.ok()
                        .body(Mono.just(dashboardData), DashboardResponse.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 根据任务ID获取里程碑统计信息
    public Mono<ServerResponse> getMilestoneStatisticsByTaskId(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        return milestoneService.getMilestoneStatistics(taskId)
                .flatMap(statistics -> ServerResponse.ok()
                        .body(Mono.just(statistics), Statistics.class));
    }
    
    // 根据任务ID获取近一年的todos创建统计
    public Mono<ServerResponse> getTodoCreationStatsByTaskId(ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        return taskService.getTodoCreationStatsByTaskId(taskId)
                .flatMap(stats -> ServerResponse.ok()
                        .body(Mono.just(stats), int[].class));
    }
}