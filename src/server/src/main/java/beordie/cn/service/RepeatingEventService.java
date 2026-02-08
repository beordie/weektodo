package beordie.cn.service;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.repository.RepeatingEventRepository;
import beordie.cn.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 重复事件服务类
 */
@Service
public class RepeatingEventService {
    
    private final RepeatingEventRepository repeatingEventRepository;
    private final TodoRepository todoRepository;
    
    public RepeatingEventService(RepeatingEventRepository repeatingEventRepository, TodoRepository todoRepository) {
        this.repeatingEventRepository = repeatingEventRepository;
        this.todoRepository = todoRepository;
    }
    
    /**
     * 保存重复事件
     */
    public Mono<RepeatingEvent> save(RepeatingEvent repeatingEvent) {
        return repeatingEventRepository.save(repeatingEvent);
    }
    
    /**
     * 根据ID获取重复事件
     */
    public Mono<RepeatingEvent> getById(String id) {
        return repeatingEventRepository.findById(id);
    }
    
    /**
     * 获取所有重复事件
     */
    public Flux<RepeatingEvent> getAll() {
        return repeatingEventRepository.findAll();
    }
    
    /**
     * 根据ID删除重复事件
     */
    public Mono<Void> deleteById(String id) {
        return repeatingEventRepository.deleteById(id);
    }
    
    /**
     * 根据日期生成重复事件对应的Todo实例
     * @param listId 日期ID，格式如：20251217
     * @return 生成的Todo列表
     */
    public Mono<List<Todo>> generateTodosForDate(String listId) {
        return Mono.fromCallable(() -> {
            // 获取该日期应该生成的重复事件ID
            Set<String> eventIds = repeatingEventRepository.getRepeatingEventIdsByListId(listId);
            
            // 检查这些重复事件是否已经生成过Todo
            List<Todo> existingTodos = Objects.requireNonNull(todoRepository.findByListId(listId)
                            .collectList()
                            .block())
                    .stream()
                    .filter(todo -> todo.getRepeatingEventId() != null)
                    .toList();
            
            Set<String> existingEventIds = existingTodos.stream()
                    .map(Todo::getRepeatingEventId)
                    .collect(Collectors.toSet());
            
            // 获取需要生成的新重复事件
            List<RepeatingEvent> eventsToGenerate = eventIds.stream()
                    .filter(eventId -> !existingEventIds.contains(eventId))
                    .map(eventId -> repeatingEventRepository.findById(eventId).block())
                    .filter(Objects::nonNull)
                    .toList();
            
            List<Todo> newTodos = eventsToGenerate.stream()
                    .map(event -> buildTodoFromEvent(event, listId))
                    .collect(Collectors.toList());
            
            // 保存新生成的Todo
            newTodos.forEach(todo -> todoRepository.save(todo).block());
            
            return newTodos;
        });
    }
    
    private Todo buildTodoFromEvent(RepeatingEvent repeatingEvent, String listId) {
        Todo origin = null;
        if (repeatingEvent.getTodoId() != null && !repeatingEvent.getTodoId().isEmpty()) {
            origin = this.todoRepository.findById(repeatingEvent.getTodoId()).block();
        }
        if (origin == null) {
            return null;
        }
        Todo copy = new Todo();
        copy.setText(origin.getText());
        copy.setChecked(0);
        copy.setListId(listId);
        copy.setDescription(origin.getDescription());
        copy.setSubTodos(origin.getSubTodos());
        copy.setColor(origin.getColor());
        copy.setPriority(origin.getPriority());
        copy.setTags(origin.getTags());
        copy.setTime(origin.getTime());
        copy.setAlarm(origin.getAlarm());
        copy.setRepeatingEventId(repeatingEvent.getId());
        copy.setTaskId(origin.getTaskId());
        copy.setMilestoneId(origin.getMilestoneId());
        copy.setCreatedAt(java.time.LocalDateTime.now());
        copy.setUpdatedAt(java.time.LocalDateTime.now());
        return copy;
    }
}
