package beordie.cn.scheduler;

import beordie.cn.service.RepeatingEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class RepeatingEventScheduler {
    private static final Logger log = LoggerFactory.getLogger(RepeatingEventScheduler.class);
    private final RepeatingEventService repeatingEventService;
    @Value("${app.repeating-scheduler.zone:Asia/Shanghai}")
    private String zone;

    public RepeatingEventScheduler(RepeatingEventService repeatingEventService) {
        this.repeatingEventService = repeatingEventService;
    }

    @Scheduled(cron = "${app.repeating-scheduler.cron:0 0 0 * * *}", zone = "${app.repeating-scheduler.zone:Asia/Shanghai}")
    public void generateDailyTodos() {
        String listId = LocalDate.now(ZoneId.of(zone)).format(DateTimeFormatter.BASIC_ISO_DATE);
        log.info("Starting repeating event generation for listId {}", listId);
        Mono.defer(() -> repeatingEventService.generateTodosForDate(listId))
                .doOnError(e -> log.error("Failed generating repeating event todos for {}: {}", listId, e.getMessage()))
                .subscribe(
                        todos -> log.info("Generated {} repeating event todos for {}", todos.size(), listId),
                        e -> log.warn("Skip repeating event generation: {}", e.getMessage())
                );
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        generateDailyTodos();
    }
}
