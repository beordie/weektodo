package beordie.cn.repository;

import beordie.cn.model.Milestone;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MilestoneRepository {
    // 查找所有里程碑
    Flux<Milestone> findAll();
    
    // 根据任务ID查找里程碑
    Flux<Milestone> findByTaskId(String taskId);
    
    // 根据ID查找里程碑
    Mono<Milestone> findById(String id);
    
    // 根据任务ID和里程碑ID查找里程碑
    Mono<Milestone> findByTaskIdAndId(String taskId, String id);
    
    // 保存里程碑
    Mono<Milestone> save(Milestone milestone);
    
    // 更新里程碑
    Mono<Milestone> update(String id, Milestone milestone);
    
    // 删除里程碑
    Mono<Void> deleteById(String id);
    
    // 根据任务ID删除所有里程碑
    Mono<Void> deleteByTaskId(String taskId);
}