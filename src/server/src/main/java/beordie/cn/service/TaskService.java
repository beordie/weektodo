package beordie.cn.service;

import beordie.cn.dashboard.dto.DashboardResponse;
import beordie.cn.dashboard.dto.DashboardStat;
import beordie.cn.dashboard.DashboardContext;
import beordie.cn.dashboard.DashboardMetricCalculator;
import beordie.cn.dashboard.DashboardScope;
import beordie.cn.dashboard.TaskDashboardContext;
import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.TaskTimeConfig;
import beordie.cn.model.Milestone;
import beordie.cn.model.Task;
import beordie.cn.repository.MilestoneRepository;
import beordie.cn.repository.TaskRepository;
import beordie.cn.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final TodoRepository todoRepository;
    private final TimeCacheService timeCacheService;
    @Autowired
    private List<DashboardMetricCalculator> dashboardCalculators;

    @Autowired
    private ConfigHandler configHandler;

    @Autowired
    public TaskService(TaskRepository taskRepository, MilestoneRepository milestoneRepository, TodoRepository todoRepository, TimeCacheService timeCacheService) {
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.todoRepository = todoRepository;
        this.timeCacheService = timeCacheService;
    }

    public Flux<Task> getAllTasks() {
        return getAllTasks(null, null, null, null);
    }

    public Flux<Task> getAllTasks(String title, String category, String sortBy, String sortOrder) {
        return taskRepository.findAll(title, category, sortBy, sortOrder)
                .concatMap(task -> 
                    milestoneRepository.findByTaskId(task.getId())
                        .collectList()
                        .flatMap(milestones -> {
                            // 计算里程碑统计信息
                            Task.MilestoneCounter counter = new Task.MilestoneCounter();
                            counter.setTotal(milestones.size());
                            counter.setDone((int) milestones.stream()
                                    .filter(milestone -> milestone.getCompleted() != null && milestone.getCompleted() == 1)
                                    .count());
                            task.setMilestoneCounter(counter);
                            return Mono.just(task);
                        })
                );
    }

    public Mono<Task> getTaskById(String id) {
        return taskRepository.findById(id)
                .flatMap(task -> 
                    milestoneRepository.findByTaskId(id)
                        .collectList()
                        .doOnNext(task::setMilestones)
                        .thenReturn(task)
                );
    }

    /**
     * 获取任务整体大盘数据
     * @return 包含任务统计信息的DashboardResponse
     */
    public Mono<DashboardResponse> getTaskDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        TaskTimeConfig taskTimeConfig = configHandler.getTaskTimeConfig();
        return getAllTasks(null, null, null, null)
                .collectList()
                .flatMap(tasks -> buildTaskTimeMap(tasks)
                        .map(taskTimeMap -> new DashboardContext(tasks, now, taskTimeConfig, timeCacheService)))
                .map(ctx -> {
                    List<DashboardStat> stats = dashboardCalculators.stream()
                            .peek(c -> logger.info("All metric id: {}", c.id()))
                            .filter(c -> c.supports(DashboardScope.GLOBAL))
                            .peek(c -> logger.info("Matched metric id: {}", c.id()))
                            .sorted(Comparator.comparingInt(DashboardMetricCalculator::order))
                            .map(c -> c.calculate(ctx))
                            .collect(Collectors.toList());
                    DashboardResponse resp = new DashboardResponse();
                    resp.setDashboardStats(stats);
                    return resp;
                });
    }

    /**
     * 根据任务ID获取任务看板数据
     * @param taskId 任务ID
     * @return 包含任务统计信息的DashboardResponse
     */
    public Mono<DashboardResponse> getTaskDashboardDataByTaskId(String taskId) {
        TaskTimeConfig taskTimeConfig = configHandler.getTaskTimeConfig();
        return getTaskById(taskId)
                .flatMap(task -> todoRepository.findByTaskId(taskId).collectList()
                .map(todos -> new TaskDashboardContext(todos, taskTimeConfig, timeCacheService))
                .map(ctx -> {
                    List<DashboardStat> stats = dashboardCalculators.stream()
                            .peek(c -> logger.info("All metric id: {}", c.id()))
                            .filter(c -> c.supports(DashboardScope.TASK))
                            .peek(c -> logger.info("Matched metric id: {}", c.id()))
                            .sorted(Comparator.comparingInt(DashboardMetricCalculator::order))
                            .map(c -> c.calculate(ctx))
                            .collect(Collectors.toList());
                    DashboardResponse resp = new DashboardResponse();
                    resp.setDashboardStats(stats);
                    return resp;
                }))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("任务不存在: " + taskId)));
    }

    private Mono<Map<String, Double>> buildTaskTimeMap(List<Task> tasks) {
        List<String> ids = tasks.stream().map(Task::getId).collect(Collectors.toList());
        Map<String, Double> map = new HashMap<>();
        return Flux.fromIterable(ids)
                .concatMap(id -> timeCacheService.getTaskTotalTimeInHours(id)
                        .map(time -> {
                            map.put(id, time);
                            return time;
                        }))
                .collectList()
                .map(list -> map);
    }

    public Mono<Task> createTask(Task task) {
        // 在创建任务时设置创建时间和更新时间
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(java.time.LocalDateTime.now());
        }
        task.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 保存任务
        return taskRepository.save(task)
                // 保存关联的里程碑
                .flatMap(savedTask -> {
                    // 如果任务包含里程碑列表
                    if (task.getMilestones() != null && !task.getMilestones().isEmpty()) {
                        // 为每个里程碑设置任务ID并保存
                        Flux<Milestone> savedMilestones = Flux.fromIterable(task.getMilestones())
                                .doOnNext(milestone -> {
                                    // 设置里程碑的任务ID
                                    milestone.setTaskId(savedTask.getId());
                                    // 设置创建时间和更新时间
                                    if (milestone.getCreatedAt() == null) {
                                        milestone.setCreatedAt(java.time.LocalDateTime.now());
                                    }
                                    milestone.setUpdatedAt(java.time.LocalDateTime.now());
                                })
                                .flatMap(milestoneRepository::save);
                        
                        // 收集保存的里程碑并设置回任务
                        return savedMilestones
                                .collectList()
                                .doOnNext(savedTask::setMilestones)
                                .thenReturn(savedTask);
                    }
                    // 如果没有里程碑，直接返回保存的任务
                    return Mono.just(savedTask);
                });
    }

    public Mono<Task> updateTask(String id, Task task) {
        // 在更新任务时设置更新时间
        task.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 首先更新任务基本信息
        return taskRepository.update(id, task)
                .flatMap(updatedTask -> {
                    // 检查是否包含里程碑信息
                    if (task.getMilestones() != null) {
                        // 获取当前任务的所有里程碑
                        return milestoneRepository.findByTaskId(id)
                                .collectList()
                                .flatMap(currentMilestones -> {
                                    // 处理新传入的里程碑：更新或创建
                                    Flux<Milestone> processedMilestones = Flux.fromIterable(task.getMilestones())
                                            .flatMap(incomingMilestone -> {
                                                // 设置任务ID和更新时间
                                                incomingMilestone.setTaskId(id);
                                                incomingMilestone.setUpdatedAt(java.time.LocalDateTime.now());
                                                
                                                // 如果里程碑有ID，更新描述
                                                if (incomingMilestone.getId() != null) {
                                                    return milestoneRepository.update(incomingMilestone.getId(), incomingMilestone);
                                                } else {
                                                    // 没有ID，创建新里程碑
                                                    if (incomingMilestone.getCreatedAt() == null) {
                                                        incomingMilestone.setCreatedAt(java.time.LocalDateTime.now());
                                                    }
                                                    return milestoneRepository.save(incomingMilestone);
                                                }
                                            });
                                    
                                    // 收集处理后的里程碑
                                    return processedMilestones.collectList()
                                            .flatMap(processedMilestonesList -> {
                                                // 找出要删除的里程碑（当前有但传入没有的）
                                                java.util.Set<String> incomingMilestoneIds = new java.util.HashSet<>();
                                                for (Milestone incomingMilestone : task.getMilestones()) {
                                                    if (incomingMilestone.getId() != null) {
                                                        incomingMilestoneIds.add(incomingMilestone.getId());
                                                    }
                                                }
                                                
                                                java.util.List<Milestone> milestonesToDelete = new java.util.ArrayList<>();
                                                for (Milestone currentMilestone : currentMilestones) {
                                                    if (!incomingMilestoneIds.contains(currentMilestone.getId())) {
                                                        milestonesToDelete.add(currentMilestone);
                                                    }
                                                }
                                                
                                                // 只要有要删除的里程碑，就执行删除逻辑
                                                if (!milestonesToDelete.isEmpty()) {
                                                    // 检查要删除的里程碑是否有todo关联
                                                    return Flux.fromIterable(milestonesToDelete)
                                                            .flatMap(milestone -> 
                                                                todoRepository.findByMilestoneId(milestone.getId())
                                                                    .collectList()
                                                                    .flatMap(todos -> {
                                                                        if (!todos.isEmpty()) {
                                                                            // 有关联的todo，报错
                                                                            return Mono.error(new IllegalArgumentException("无法删除里程碑'" + milestone.getTitle() + "'，它与待办事项存在关联"));
                                                                        }
                                                                        // 没有关联的todo，删除里程碑
                                                                        return milestoneRepository.deleteById(milestone.getId())
                                                                            .then(Mono.just(milestone));
                                                                    })
                                                            )
                                                            .collectList() // 收集删除的里程碑（虽然不需要结果，但需要等待操作完成）
                                                            .flatMap(ignored -> {
                                                                // 获取最新的里程碑列表
                                                                return milestoneRepository.findByTaskId(id)
                                                                        .collectList()
                                                                        .doOnNext(updatedTask::setMilestones)
                                                                        .thenReturn(updatedTask);
                                                            });
                                                } else {
                                                    // 没有要删除的里程碑，直接设置处理后的里程碑列表
                                                    updatedTask.setMilestones(processedMilestonesList);
                                                    return Mono.just(updatedTask);
                                                }
                                            });
                                });
                    }
                    // 如果没有里程碑信息，需要检查并删除所有已存在的里程碑
                    return milestoneRepository.findByTaskId(id)
                            .collectList()
                            .flatMap(currentMilestones -> {
                                // 如果没有已存在的里程碑，直接返回更新后的任务
                                if (currentMilestones.isEmpty()) {
                                    updatedTask.setMilestones(null);
                                    return Mono.just(updatedTask);
                                }
                                
                                // 检查每个要删除的里程碑是否有todo关联
                                return Flux.fromIterable(currentMilestones)
                                        .flatMap(milestone -> 
                                            todoRepository.findByMilestoneId(milestone.getId())
                                                .collectList()
                                                .flatMap(todos -> {
                                                    if (!todos.isEmpty()) {
                                                        // 有关联的todo，报错
                                                        return Mono.error(new IllegalArgumentException("无法删除里程碑'" + milestone.getTitle() + "'，它与待办事项存在关联"));
                                                    }
                                                    // 没有关联的todo，删除里程碑
                                                    return milestoneRepository.deleteById(milestone.getId())
                                                        .then(Mono.just(milestone));
                                                })
                                        )
                                        .collectList() // 收集删除的里程碑（虽然不需要结果，但需要等待操作完成）
                                        .flatMap(ignored -> {
                                            // 设置任务的里程碑列表为空
                                            updatedTask.setMilestones(null);
                                            return Mono.just(updatedTask);
                                        });
                            });
                });
    }

    public Mono<Void> deleteTask(String id) {
        return taskRepository.deleteById(id);
    }
    
    /**
     * 统计指定taskId近一年的todos创建情况
     * @param taskId 任务ID
     * @return 按日期索引的创建数量统计数组（长度365，代表近一年每一天的创建数量）
     */
    public Mono<int[]> getTodoCreationStatsByTaskId(String taskId) {
        // 获取从今天开始往前面数365天的开始日期，格式如：20250101
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate oneYearAgo = today.minusDays(365);
        String oneYearAgoStr = oneYearAgo.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        
        // 创建一个长度为365的数组，初始值都为0
        int[] stats = new int[365];
        
        // 使用新的查询方法，获取按listId分组的统计数据
        return todoRepository.findByTaskIdAndListIdGreaterThanGroupByListId(taskId, oneYearAgoStr)
                // 使用reduce操作符来累积统计结果，确保所有处理都完成
                .reduce(stats, (result, entry) -> {
                    String listId = entry.getKey(); // 格式如：20250101
                    int count = entry.getValue();
                    
                    try {
                        // 将listId转换为LocalDate
                        java.time.LocalDate date = java.time.LocalDate.parse(listId, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
                        // 计算距离一年前的天数差
                        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(oneYearAgo, date);
                        // 确保daysDiff在0-364范围内
                        if (daysDiff >= 0 && daysDiff < 365) {
                            // 在对应位置的数组元素上设置数量
                            result[(int) daysDiff] = count;
                        }
                    } catch (Exception e) {
                        // 如果日期解析失败，忽略该数据
                        System.err.println("解析listId失败: " + listId + ", 错误: " + e.getMessage());
                    }
                    return result;
                })
                // 打印统计结果
                .doOnNext(result -> {
                    int nonZeroCount = 0;
                    for (int i = 0; i < result.length; i++) {
                        if (result[i] > 0) {
                            nonZeroCount++;
                        }
                    }
                    System.out.println("TaskService.getTodoCreationStatsByTaskId: 返回统计数据，非零值数量=" + nonZeroCount);
                });
    }
}
