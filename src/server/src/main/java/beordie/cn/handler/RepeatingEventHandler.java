package beordie.cn.handler;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.service.RepeatingEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(RepeatingEventHandler.class);
    
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
        return repeatingEventService.getByTodoAndEventId(todoId, id)
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
        logger.info("Received request to create repeating event for todoId: {}", todoId);
        
        return request.bodyToMono(RepeatingEvent.class)
                .doOnNext(repeatingEvent -> {
                    logger.info("Parsed repeating event: {}", repeatingEvent);
                    repeatingEvent.setTodoId(todoId);
                    logger.info("Set todoId to repeating event: {}", repeatingEvent);
                })
                .flatMap(repeatingEvent -> {
                    logger.info("Saving repeating event: {}", repeatingEvent);
                    return repeatingEventService.save(repeatingEvent);
                })
                .doOnNext(savedEvent -> {
                    logger.info("Saved repeating event successfully: {}", savedEvent);
                })
                .flatMap(savedEvent -> {
                    logger.info("Preparing response with saved event: {}", savedEvent);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(fromValue(savedEvent));
                })
                .doOnError(error -> {
                    logger.error("Error creating repeating event: {}", error.getMessage(), error);
                });
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
