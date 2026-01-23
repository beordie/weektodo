package beordie.cn.service;

import beordie.cn.model.Milestone;
import beordie.cn.model.Todo;
import beordie.cn.repository.MilestoneRepository;
import beordie.cn.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final TodoRepository todoRepository;

    @Autowired
    public MilestoneService(MilestoneRepository milestoneRepository, TodoRepository todoRepository) {
        this.milestoneRepository = milestoneRepository;
        this.todoRepository = todoRepository;
    }

    // 获取所有里程碑
    public Flux<Milestone> getAllMilestones() {
        return milestoneRepository.findAll();
    }

    // 根据任务ID获取所有里程碑
    public Flux<Milestone> getMilestonesByTaskId(String taskId) {
        return milestoneRepository.findByTaskId(taskId);
    }

    // 根据ID获取里程碑
    public Mono<Milestone> getMilestoneById(String id) {
        return milestoneRepository.findById(id);
    }

    // 根据任务ID和里程碑ID获取里程碑
    public Mono<Milestone> getMilestoneByTaskIdAndId(String taskId, String id) {
        return milestoneRepository.findByTaskIdAndId(taskId, id);
    }

    // 创建里程碑
    public Mono<Milestone> createMilestone(Milestone milestone) {
        // 设置创建时间和更新时间
        if (milestone.getCreatedAt() == null) {
            milestone.setCreatedAt(java.time.LocalDateTime.now());
        }
        milestone.setUpdatedAt(java.time.LocalDateTime.now());
        return milestoneRepository.save(milestone);
    }

    // 更新里程碑
    public Mono<Milestone> updateMilestone(String id, Milestone milestone) {
        // 设置更新时间
        milestone.setUpdatedAt(java.time.LocalDateTime.now());
        return milestoneRepository.update(id, milestone);
    }

    // 删除里程碑
    public Mono<Void> deleteMilestone(String id) {
        return milestoneRepository.deleteById(id);
    }

    // 根据任务ID删除所有里程碑
    public Mono<Void> deleteMilestonesByTaskId(String taskId) {
        return milestoneRepository.deleteByTaskId(taskId);
    }

    // 获取指定任务的里程碑统计信息
    public Mono<beordie.cn.dashboard.milestone.Statistics> getMilestoneStatistics(String taskId) {
        return milestoneRepository.findByTaskId(taskId)
                .flatMap(milestone -> {
                    // 获取当前里程碑的所有todos
                    return todoRepository.findByMilestoneId(milestone.getId())
                            .collectList()
                            .map(todos -> {
                                // 统计todos
                                int totalTodos = todos.size();
                                int completedTodos = (int) todos.stream()
                                        .filter(todo -> todo.checkCompleted())
                                        .count();
                                int pendingTodos = totalTodos - completedTodos;
                                double completionRate = totalTodos > 0 ? (double) completedTodos / totalTodos * 100 : 0.0;

                                // 创建Item对象
                                beordie.cn.dashboard.milestone.Item item = new beordie.cn.dashboard.milestone.Item();
                                item.setName(milestone.getTitle());
                                item.setTotalTodos(totalTodos);
                                item.setCompletedTodos(completedTodos);
                                item.setPendingTodos(pendingTodos);
                                item.setCompletionRate(completionRate);

                                return new Tuple(milestone, item);
                            });
                })
                .collectList()
                // 使用map而不是flatMap，确保始终返回Statistics对象
                .map(tuples -> {
                    int total = tuples.size();
                    double averageCompletionRate = 0.0;
                    java.util.List<beordie.cn.dashboard.milestone.Item> milestoneItems = new java.util.ArrayList<>();

                    if (total > 0) {
                        double sumCompletionRate = 0.0;
                        for (Tuple tuple : tuples) {
                            milestoneItems.add(tuple.item);
                            sumCompletionRate += tuple.item.getCompletionRate();
                        }
                        averageCompletionRate = sumCompletionRate / total;
                    }

                    // 创建Summary对象
                    beordie.cn.dashboard.milestone.Summary summary = new beordie.cn.dashboard.milestone.Summary();
                    summary.setTotal(total);
                    summary.setAverageCompletionRate(averageCompletionRate);

                    // 创建Statistics对象
                    beordie.cn.dashboard.milestone.Statistics statistics = new beordie.cn.dashboard.milestone.Statistics();
                    statistics.setSummary(summary);
                    statistics.setMilestones(milestoneItems);

                    return statistics;
                })
                // 确保无论如何都返回一个Statistics对象，而不是空的Mono
                .defaultIfEmpty(createEmptyStatistics());
    }
    
    // 创建空的Statistics对象
    private beordie.cn.dashboard.milestone.Statistics createEmptyStatistics() {
        beordie.cn.dashboard.milestone.Summary summary = new beordie.cn.dashboard.milestone.Summary();
        summary.setTotal(0);
        summary.setAverageCompletionRate(0.0);
        
        beordie.cn.dashboard.milestone.Statistics statistics = new beordie.cn.dashboard.milestone.Statistics();
        statistics.setSummary(summary);
        statistics.setMilestones(new java.util.ArrayList<>());
        
        return statistics;
    }

    // 内部辅助类，用于在flatMap中传递数据
    private static class Tuple {
        Milestone milestone;
        beordie.cn.dashboard.milestone.Item item;

        Tuple(Milestone milestone, beordie.cn.dashboard.milestone.Item item) {
            this.milestone = milestone;
            this.item = item;
        }
    }
}