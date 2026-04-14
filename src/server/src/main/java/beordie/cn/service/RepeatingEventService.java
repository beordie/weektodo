package beordie.cn.service;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.repository.RepeatingEventRepository;
import beordie.cn.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
    
    private static final Logger log = LoggerFactory.getLogger(RepeatingEventService.class);
    private final RepeatingEventRepository repeatingEventRepository;
    private final TodoService todoService;
    
    public RepeatingEventService(RepeatingEventRepository repeatingEventRepository, @Lazy TodoService todoService) {
        this.repeatingEventRepository = repeatingEventRepository;
        this.todoService = todoService;
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
        if (eventId == null || eventId.isEmpty()) {
            return Mono.empty();
        }
        return repeatingEventRepository.findById(eventId)
                .filter(ev -> ev != null && ev.getTodoId() != null && ev.getTodoId().equals(todoId));
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
            log.info("=================================");
            log.info("RepeatingEventService.generateTodosForDate: Starting for listId {}", listId);
            log.info("=================================");
            
            LocalDate targetDate = LocalDate.parse(listId, DateTimeFormatter.BASIC_ISO_DATE);
            log.info("RepeatingEventService: targetDate {}", targetDate);
            
            List<Todo> existingTodos = Objects.requireNonNull(todoService.getTodosByListIdDirectly(listId)
                            .collectList()
                            .block())
                            .stream()
                            .toList();
            log.info("RepeatingEventService: existingTodos: {}", existingTodos.size());

            List<RepeatingEvent> allEvents = Objects.requireNonNull(repeatingEventRepository.findAll().collectList().block());
            log.info("RepeatingEventService: allEvents from DB: {}", allEvents.size());

            // 1) 预设当天规则内的事件，先构建内存中的待创建 Todo（不触发数据库）
            List<RepeatingEvent> occurEvents = allEvents.stream()
                    .filter(e -> occursOnDate(e, targetDate))
                    .toList();
            log.info("RepeatingEventService: occurEvents on targetDate: {}", occurEvents.size());

            List<Todo> preCreatedTodos = occurEvents.stream()
                    .map(event -> buildTodoFromEvent(event, listId))
                    .filter(Objects::nonNull)
                    .toList();
            log.info("RepeatingEventService: preCreatedTodos: {}", preCreatedTodos.size());

            // 2) 与当天已存在的 Todo 进行对比（按 repeatingEventId），剔除重复项
            List<Todo> needCreate = preCreatedTodos.stream()
                    .filter(t -> existingTodos.stream().noneMatch(todo -> todo.equals(t)))
                    .collect(Collectors.toList());
            log.info("RepeatingEventService: needCreate after deduplication: {}", needCreate.size());

            // 3) 将剩余的预创建 Todo 插入数据库
            log.info("RepeatingEventService: Saving {} todos to DB", needCreate.size());
            needCreate.forEach(todo -> {
                todoService.createTodo(todo).block();
            });
            
            log.info("RepeatingEventService.generateTodosForDate: Done, generated {} todos", needCreate.size());
            return needCreate;
        });
    }
    
    private Todo buildTodoFromEvent(RepeatingEvent repeatingEvent, String listId) {
        Todo origin = null;
        if (repeatingEvent.getTodoId() != null && !repeatingEvent.getTodoId().isEmpty()) {
            origin = this.todoService.getTodoById(repeatingEvent.getTodoId()).block();
        }
        if (origin == null) {
            return null;
        }
        Todo copy = new Todo();
        copy.setText(origin.getText());
        copy.setChecked(0);
        copy.setListId(listId);
        copy.setDescription(origin.getDescription());
        copy.setColor(origin.getColor());
        copy.setPriority(origin.getPriority());
        copy.setTags(origin.getTags());
        copy.setTime(origin.getTime());
        copy.setAlarm(origin.getAlarm());
        copy.setTaskId(origin.getTaskId());
        copy.setMilestoneId(origin.getMilestoneId());
        copy.setCreatedAt(java.time.LocalDateTime.now());
        copy.setUpdatedAt(java.time.LocalDateTime.now());
        return copy;
    }

    private String cleanRepeatingRule(String rawRule) {
        if (rawRule == null || rawRule.isBlank()) {
            return rawRule;
        }
        // 去掉前面的 DTSTART 等前缀，只保留 RRULE 部分或整行中的规则
        for (String line : rawRule.split("[\\r\\n]+")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("RRULE:")) {
                return trimmed.substring("RRULE:".length()).trim();
            }
            if (!trimmed.contains(":")) {
                // 没有前缀，直接返回
                return trimmed;
            }
        }
        // fallback：返回原值
        return rawRule;
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
            String ruleStr = cleanRepeatingRule(event.getRepeatingRule());
            log.info("RepeatingEventService.occursOnDate: clean rule='{}'", ruleStr);
            Recur recur = new Recur(ruleStr);
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
