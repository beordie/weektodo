package beordie.cn.service;

import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.TaskTimeConfig;
import beordie.cn.model.Milestone;
import beordie.cn.model.Task;
import beordie.cn.model.Todo;
import beordie.cn.repository.MilestoneRepository;
import beordie.cn.repository.TaskRepository;
import beordie.cn.repository.TodoRepository;
import beordie.cn.service.TimeCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final TodoRepository todoRepository;
    private final TimeCacheService timeCacheService;

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
     * @return 包含任务统计信息的Map
     */
    public Mono<Map<String, Object>> getTaskDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        
        // 使用ConfigHandler获取时间阈值配置
        TaskTimeConfig taskTimeConfig = configHandler.getTaskTimeConfig();
        int upcomingThresholdDays = taskTimeConfig.getUpcomingThresholdDays();
        int overdueThresholdDays = taskTimeConfig.getOverdueThresholdDays();
        
        // 获取所有任务并进行统计
        return getAllTasks(null, null, null, null)
                .collectList()
                .flatMap(tasks -> {
                    // 基础统计
                    int totalTasks = tasks.size();
                    AtomicInteger completedTasks = new AtomicInteger(0);
                    AtomicInteger upcomingTasks = new AtomicInteger(0);
                    AtomicInteger overdueTasks = new AtomicInteger(0);
                    AtomicReference<Double> totalTodoTime = new AtomicReference<>(0.0);
                    
                    // 收集所有任务ID，用于批量查询时间
                    List<String> taskIds = tasks.stream()
                            .map(Task::getId)
                            .collect(Collectors.toList());
                    
                    // 收集任务时间统计
                    Map<String, Double> taskTimeMap = new HashMap<>();
                    
                    // 查询每个任务的总时间
                    return Flux.fromIterable(taskIds)
                            .concatMap(taskId -> timeCacheService.getTaskTotalTimeInHours(taskId)
                                    .map(time -> {
                                        taskTimeMap.put(taskId, time);
                                        return time;
                                    }))
                            .collectList()
                            .map(timeList -> {
                                // 遍历任务进行统计
                                for (Task task : tasks) {
                                    // 已完成任务统计
                                    if (task.getCompleted() != null && task.getCompleted() == 1) {
                                        completedTasks.incrementAndGet();
                                    }
                                    
                                    // 时间相关统计
                                    LocalDate endDate = task.getEndDate();
                                    if (endDate != null) {
                                        // 转换当前时间为LocalDate进行比较
                                        LocalDate nowDate = now.toLocalDate();
                                        // 逾期任务统计：任务未完成且当前时间超过结束时间加上逾期阈值
                                        if ((task.getCompleted() == null || task.getCompleted() != 1) && endDate.isBefore(nowDate.minusDays(overdueThresholdDays))) {
                                            overdueTasks.incrementAndGet();
                                        }
                                        
                                        // 即将到期任务统计
                                        if ((task.getCompleted() == null || task.getCompleted() != 1) && 
                                            endDate.isAfter(nowDate) && 
                                            endDate.isBefore(nowDate.plusDays(upcomingThresholdDays))) {
                                            upcomingTasks.incrementAndGet();
                                        }
                                    }
                                    
                                    // 添加该任务的todo时间总和
                                    totalTodoTime.updateAndGet(current -> current + taskTimeMap.getOrDefault(task.getId(), 0.0));
                                }
                                
                                // 构建返回结果
                                Map<String, Object> dashboardData = new HashMap<>();
                                dashboardData.put("totalTasks", totalTasks);
                                dashboardData.put("completedTasks", completedTasks.get());
                                dashboardData.put("upcomingTasks", upcomingTasks.get());
                                dashboardData.put("overdueTasks", overdueTasks.get());
                                dashboardData.put("totalTodoTime", totalTodoTime.get());
                                
                                return dashboardData;
                            });
                });
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
}
