package beordie.cn.repository.impl;

import beordie.cn.mapper.RepeatingEventMapper;
import beordie.cn.mapper.TodoMapper;
import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import beordie.cn.repository.RepeatingEventRepository;
import lombok.RequiredArgsConstructor;
import java.util.Calendar;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 重复事件仓库的MyBatis Plus实现
 */
@Repository
public class MyBatisPlusRepeatingEventRepository implements RepeatingEventRepository {
    private final RepeatingEventMapper repeatingEventMapper;
    private final TodoMapper todoMapper;

    public MyBatisPlusRepeatingEventRepository(RepeatingEventMapper repeatingEventMapper, TodoMapper todoMapper) {
        this.repeatingEventMapper = repeatingEventMapper;
        this.todoMapper = todoMapper;
    }

    @Override
    public Mono<RepeatingEvent> save(RepeatingEvent repeatingEvent) {
        return Mono.fromCallable(() -> {
            LocalDateTime now = LocalDateTime.now();
            if (repeatingEvent.getCreatedAt() == null) {
                repeatingEvent.setCreatedAt(now);
            }
            repeatingEvent.setUpdatedAt(now);
            repeatingEventMapper.insert(repeatingEvent);
            return repeatingEvent;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<RepeatingEvent> findById(String id) {
        return Mono.fromCallable(() -> repeatingEventMapper.selectById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Flux<RepeatingEvent> findAll() {
        return Flux.fromIterable(repeatingEventMapper.selectList(null))
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> repeatingEventMapper.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
    
    @Override
    public Set<String> getRepeatingEventIdsByListId(String listId) {
        // 从所有重复事件中筛选出在指定日期应该生成的事件
        List<RepeatingEvent> allEvents = repeatingEventMapper.selectList(null);
        Set<String> result = new HashSet<>();
        
        LocalDate targetDate = LocalDate.parse(listId, DateTimeFormatter.BASIC_ISO_DATE);
        
        for (RepeatingEvent event : allEvents) {
            try {
                if (shouldGenerateOnDate(event, targetDate)) {
                    result.add(event.getId());
                }
            } catch (Exception e) {
                // 处理异常，跳过无效的重复事件
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    
    /**
     * 判断重复事件是否应该在指定日期生成
     * @param repeatingEvent 重复事件
     * @param targetDate 目标日期
     * @return 是否应该生成
     */
    private boolean shouldGenerateOnDate(RepeatingEvent repeatingEvent, LocalDate targetDate) {
        String rruleStr = repeatingEvent.getRepeatingRule();
        Date startDate = repeatingEvent.getStartDate();
        Date endDate = repeatingEvent.getEndDate();
        
        // 检查目标日期是否在事件的时间范围内
        LocalDate startLocalDate = startDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate != null ? endDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null;
        
        if (targetDate.isBefore(startLocalDate)) {
            return false;
        }
        
        if (endLocalDate != null && targetDate.isAfter(endLocalDate)) {
            return false;
        }
        
        // 没有重复规则，只在开始日期生成
        if (rruleStr == null || rruleStr.isEmpty()) {
            return targetDate.equals(startLocalDate);
        }
        
        // 简单实现：检查目标日期是否是每周重复
        if (rruleStr.contains("FREQ=WEEKLY")) {
            // 计算目标日期与开始日期的天数差
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, targetDate);
            return daysDiff >= 0 && daysDiff % 7 == 0;
        }
        
        return false;
    }
    

}
