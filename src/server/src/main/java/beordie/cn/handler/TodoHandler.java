package beordie.cn.handler;

import beordie.cn.model.Todo;
import beordie.cn.service.TodoService;
import beordie.cn.web.PageQuery;
import beordie.cn.web.TodoPageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class TodoHandler {
    private final TodoService todoService;

    @Autowired
    public TodoHandler(TodoService todoService) {
        this.todoService = todoService;
    }

    // 获取所有待办事项
    public Mono<ServerResponse> getAllTodos(ServerRequest request) {
        TodoPageQuery q = TodoPageQuery.from(request);
        return ServerResponse.ok().body(todoService.getTodos(q), Todo.class);
    }

    // 根据ID获取待办事项
    public Mono<ServerResponse> getTodoById(ServerRequest request) {
        String id = request.pathVariable("id");
        return todoService.getTodoById(id)
                .flatMap(todo -> ServerResponse.ok().body(fromValue(todo)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 根据列表ID获取待办事项
    public Mono<ServerResponse> getTodosByListId(ServerRequest request) {
        String listId = request.pathVariable("listId");
        return ServerResponse.ok()
                .body(todoService.getTodosByListId(listId), Todo.class);
    }

    // 根据任务获取待办事项
    public Mono<ServerResponse> getTodosByTask(ServerRequest request) {
        String task = request.queryParam("task").orElse("");
        return ServerResponse.ok()
                .body(todoService.getTodosByTask(task), Todo.class);
    }

    // 根据里程碑获取待办事项
    public Mono<ServerResponse> getTodosByMilestone(ServerRequest request) {
        String milestone = request.queryParam("milestone").orElse("");
        return ServerResponse.ok()
                .body(todoService.getTodosByMilestone(milestone), Todo.class);
    }

    // 创建待办事项
    public Mono<ServerResponse> createTodo(ServerRequest request) {
        return request.bodyToMono(Todo.class)
                .flatMap(todoService::createTodo)
                .flatMap(savedTodo -> ServerResponse.status(HttpStatus.CREATED)
                        .body(fromValue(savedTodo)));
    }

    // 更新待办事项
    public Mono<ServerResponse> updateTodo(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(Todo.class)
                .flatMap(todo -> todoService.updateTodo(id, todo))
                .flatMap(updatedTodo -> ServerResponse.ok()
                        .body(fromValue(updatedTodo)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 删除待办事项
    public Mono<ServerResponse> deleteTodo(ServerRequest request) {
        String id = request.pathVariable("id");
        return todoService.deleteTodo(id)
                .then(ServerResponse.noContent().build());
    }

    // 切换待办事项的完成状态
    public Mono<ServerResponse> toggleTodo(ServerRequest request) {
        String id = request.pathVariable("id");
        return todoService.toggleTodo(id)
                .flatMap(updatedTodo -> ServerResponse.ok()
                        .body(fromValue(updatedTodo)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 切换子任务的完成状态
    public Mono<ServerResponse> toggleSubTask(ServerRequest request) {
        String id = request.pathVariable("id");
        int index = Integer.parseInt(request.pathVariable("index"));
        return todoService.toggleSubTask(id, index)
                .flatMap(updatedTodo -> ServerResponse.ok()
                        .body(fromValue(updatedTodo)))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(IllegalArgumentException.class, e -> ServerResponse.badRequest().body(fromValue(e.getMessage())));
    }
    
}
