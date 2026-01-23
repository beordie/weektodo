package beordie.cn.config;

import beordie.cn.handler.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TaskRouterConfig {
    private static final Logger logger = LoggerFactory.getLogger(TaskRouterConfig.class);
    private final TaskHandler taskHandler;

    @Autowired
    public TaskRouterConfig(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
        logger.info("TaskRouterConfig is being initialized");
    }

    @Bean
    public RouterFunction<ServerResponse> taskRoutes() {
        // 重新组织路由，按照合理的顺序排列
        return RouterFunctions
                // 获取所有任务，支持查询和排序
                .route(GET("/api/tasks").and(accept(APPLICATION_JSON)), taskHandler::getAllTasks)
                // 获取任务整体大盘数据
                .andRoute(GET("/api/tasks/dashboard").and(accept(APPLICATION_JSON)), taskHandler::getTaskDashboardData)
                .andRoute(GET("/api/tasks/{taskId}/dashboard").and(accept(APPLICATION_JSON)), taskHandler::getTaskDashboardDataByTaskId)
                // 根据ID获取任务
                .andRoute(GET("/api/tasks/{id}").and(accept(APPLICATION_JSON)), taskHandler::getTaskById)
                // 创建任务
                .andRoute(POST("/api/tasks")
                        .and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)), taskHandler::createTask)
                // 更新任务
                .andRoute(PUT("/api/tasks/{id}")
                        .and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)), taskHandler::updateTask)
                // 删除任务
                .andRoute(DELETE("/api/tasks/{id}"), taskHandler::deleteTask)
                // 任务里程碑相关路由
                .andRoute(GET("/api/tasks/{taskId}/milestones").and(accept(APPLICATION_JSON)), taskHandler::getMilestonesByTaskId)
                // 获取指定任务的里程碑统计信息 - 放在milestoneId路由之前，避免冲突
                .andRoute(GET("/api/tasks/{taskId}/milestones/statistics").and(accept(APPLICATION_JSON)), taskHandler::getMilestoneStatisticsByTaskId)
                // 获取指定任务的todos创建统计信息
                .andRoute(GET("/api/tasks/{taskId}/todos/stats").and(accept(APPLICATION_JSON)), taskHandler::getTodoCreationStatsByTaskId)
                .andRoute(GET("/api/tasks/{taskId}/milestones/{milestoneId}").and(accept(APPLICATION_JSON)), taskHandler::getMilestoneByTaskIdAndId)
                .andRoute(POST("/api/tasks/{taskId}/milestones")
                        .and(contentType(APPLICATION_JSON)), taskHandler::createMilestone)
                .andRoute(PUT("/api/tasks/{taskId}/milestones/{milestoneId}")
                        .and(contentType(APPLICATION_JSON)), taskHandler::updateMilestone)
                .andRoute(DELETE("/api/tasks/{taskId}/milestones/{milestoneId}"), taskHandler::deleteMilestone);
    }
}
