package beordie.cn.config;

import beordie.cn.handler.RepeatingEventHandler;
import beordie.cn.handler.TodoHandler;
import beordie.cn.service.RepeatingEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TodoRouterConfig {
    private final TodoHandler todoHandler;

    @Autowired
    public TodoRouterConfig(TodoHandler todoHandler) {
        this.todoHandler = todoHandler;
    }

    @Bean
    public RepeatingEventHandler repeatingEventHandler(RepeatingEventService repeatingEventService) {
        return new RepeatingEventHandler(repeatingEventService);
    }

    @Bean
    public RouterFunction<ServerResponse> todoRoutes(RepeatingEventHandler repeatingEventHandler) {
        return RouterFunctions
                // 获取所有待办事项
                .route(GET("/api/todos").and(accept(APPLICATION_JSON)), todoHandler::getAllTodos)
                // 重复事件相关路由（需放在 /api/todos/{id} 之前，避免被通配匹配）
                .andRoute(GET("/api/todos/repeating-events"), repeatingEventHandler::getAllRepeatingEvents)
                .andRoute(GET("/api/todos/{todoId}/repeating-events/{id}"), repeatingEventHandler::getRepeatingEventById)
                .andRoute(POST("/api/todos/{todoId}/repeating-events"), repeatingEventHandler::createRepeatingEvent)
                .andRoute(DELETE("/api/todos/{todoId}/repeating-events/{id}"), repeatingEventHandler::deleteRepeatingEvent)
                // 根据ID获取待办事项
                .andRoute(GET("/api/todos/{id}").and(accept(APPLICATION_JSON)), todoHandler::getTodoById)
                // 根据列表ID获取待办事项
                .andRoute(GET("/api/todos/list/{listId}").and(accept(APPLICATION_JSON)), todoHandler::getTodosByListId)
                // 根据任务获取待办事项
                .andRoute(GET("/api/todos/search/by-task").and(accept(APPLICATION_JSON)), todoHandler::getTodosByTask)
                // 根据里程碑获取待办事项
                .andRoute(GET("/api/todos/search/by-milestone").and(accept(APPLICATION_JSON)), todoHandler::getTodosByMilestone)
                // 创建待办事项
                .andRoute(POST("/api/todos").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)), todoHandler::createTodo)
                // 更新待办事项
                .andRoute(PUT("/api/todos/{id}").and(contentType(APPLICATION_JSON)).and(accept(APPLICATION_JSON)), todoHandler::updateTodo)
                // 删除待办事项
                .andRoute(DELETE("/api/todos/{id}"), todoHandler::deleteTodo)
                // 切换待办事项的完成状态
                .andRoute(PATCH("/api/todos/{id}/toggle"), todoHandler::toggleTodo)
                // 切换子任务的完成状态
                .andRoute(PATCH("/api/todos/{id}/subtask/{index}/toggle"), todoHandler::toggleSubTask);
    }
}
