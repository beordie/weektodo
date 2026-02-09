package beordie.cn.service;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.repository.RepeatingEventRepository;
import beordie.cn.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
    
    public Mono<RepeatingEvent> getByTodoAndEventId(String todoId, String eventId) {
        return todoRepository.findById(todoId)
                .flatMap(todo -> {
                    if (todo != null && eventId != null && eventId.equals(todo.getRepeatingEventId())) {
                        return repeatingEventRepository.findById(eventId);
                    }
                    return Mono.empty();
                });
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
            LocalDate targetDate = LocalDate.parse(listId, DateTimeFormatter.BASIC_ISO_DATE);
            List<Todo> existingTodos = Objects.requireNonNull(todoRepository.findByListId(listId)
                            .collectList()
                            .block())
                    .stream()
                    .filter(todo -> todo.getRepeatingEventId() != null)
                    .toList();
            
            Set<String> existingEventIds = existingTodos.stream()
                    .map(Todo::getRepeatingEventId)
                    .collect(Collectors.toSet());
            
            List<RepeatingEvent> allEvents = Objects.requireNonNull(repeatingEventRepository.findAll().collectList().block());
            List<RepeatingEvent> eventsToGenerate = allEvents.stream()
                    .filter(e -> occursOnDate(e, targetDate))
                    .filter(e -> !existingEventIds.contains(e.getId()))
                    .toList();
            
            List<Todo> newTodos = eventsToGenerate.stream()
                    .map(event -> buildTodoFromEvent(event, listId))
                    .collect(Collectors.toList());
            
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

    private boolean occursOnDate(RepeatingEvent event, LocalDate date) {
        if (event == null || event.getRepeatingRule() == null || event.getStartDate() == null) {
            return false;
        }
        if (event.getEndDate() != null) {
            LocalDate end = event.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (date.isAfter(end)) {
                return false;
            }
        }
        try {
            Recur recur = new Recur(event.getRepeatingRule());
            DateTime dtStart = new DateTime(event.getStartDate());
            Date dayStartUtil = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dayEndUtil = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTime periodStart = new DateTime(dayStartUtil);
            DateTime periodEnd = new DateTime(dayEndUtil);
            DateList dates = recur.getDates(dtStart, periodStart, periodEnd, Value.DATE_TIME);
            return dates != null && !dates.isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }
}
