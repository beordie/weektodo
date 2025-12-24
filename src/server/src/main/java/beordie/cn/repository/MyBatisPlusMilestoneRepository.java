package beordie.cn.repository;

import beordie.cn.mapper.MilestoneMapper;
import beordie.cn.model.Milestone;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
public class MyBatisPlusMilestoneRepository implements MilestoneRepository {
    private final MilestoneMapper milestoneMapper;

    @Autowired
    public MyBatisPlusMilestoneRepository(MilestoneMapper milestoneMapper) {
        this.milestoneMapper = milestoneMapper;
    }

    @Override
    public Flux<Milestone> findAll() {
        return Mono.fromSupplier(() -> milestoneMapper.selectList(null))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Milestone> findByTaskId(String taskId) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Milestone> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", taskId);
            return milestoneMapper.selectList(queryWrapper);
        })
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Milestone> findById(String id) {
        return Mono.fromSupplier(() -> milestoneMapper.selectById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Milestone> findByTaskIdAndId(String taskId, String id) {
        return Mono.fromSupplier(() -> {
            QueryWrapper<Milestone> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", taskId)
                        .eq("id", id);
            return milestoneMapper.selectOne(queryWrapper);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Milestone> save(Milestone milestone) {
        return Mono.fromCallable(() -> {
            milestoneMapper.insert(milestone);
            return milestone;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Milestone> update(String id, Milestone milestone) {
        return Mono.fromCallable(() -> {
            milestone.setId(id);
            milestoneMapper.updateById(milestone);
            return milestone;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> milestoneMapper.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> deleteByTaskId(String taskId) {
        return Mono.fromRunnable(() -> {
            QueryWrapper<Milestone> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("task_id", taskId);
            milestoneMapper.delete(queryWrapper);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}