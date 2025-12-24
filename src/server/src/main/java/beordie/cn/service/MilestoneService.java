package beordie.cn.service;

import beordie.cn.model.Milestone;
import beordie.cn.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;

    @Autowired
    public MilestoneService(MilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
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
}