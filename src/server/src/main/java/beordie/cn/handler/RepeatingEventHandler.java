package beordie.cn.handler;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.service.RepeatingEventService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * 重复事件处理器
 */
public class RepeatingEventHandler {
    
    private final RepeatingEventService repeatingEventService;
    
    public RepeatingEventHandler(RepeatingEventService repeatingEventService) {
        this.repeatingEventService = repeatingEventService;
    }
    
    /**
     * 获取所有重复事件
     */
    public Mono<ServerResponse> getAllRepeatingEvents(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(repeatingEventService.getAll(), RepeatingEvent.class);
    }
    
    /**
     * 根据ID获取重复事件
     */
    public Mono<ServerResponse> getRepeatingEventById(ServerRequest request) {
        String todoId = request.pathVariable("todoId");
        String id = request.pathVariable("id");
        return repeatingEventService.getById(id)
                .flatMap(repeatingEvent -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(repeatingEvent)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    /**
     * 创建重复事件
     */
    public Mono<ServerResponse> createRepeatingEvent(ServerRequest request) {
        String todoId = request.pathVariable("todoId");
        return request.bodyToMono(RepeatingEvent.class)
                .doOnNext(repeatingEvent -> repeatingEvent.setTodoId(todoId))
                .flatMap(repeatingEventService::save)
                .flatMap(savedEvent -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(savedEvent)));
    }
    
    /**
     * 删除重复事件
     */
    public Mono<ServerResponse> deleteRepeatingEvent(ServerRequest request) {
        String todoId = request.pathVariable("todoId");
        String id = request.pathVariable("id");
        return repeatingEventService.deleteById(id)
                .then(ServerResponse.noContent().build());
    }
    
    /**
     * 根据日期生成重复事件对应的Todo
     */
    public Mono<ServerResponse> generateTodosForDate(ServerRequest request) {
        String todoId = request.pathVariable("todoId");
        String listId = request.pathVariable("listId");
        return repeatingEventService.generateTodosForDate(listId)
                .flatMap(todos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(todos)));
    }
}
