package beordie.cn.repository;

import beordie.cn.model.RepeatingEvent;
import beordie.cn.model.Todo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * 重复事件仓库接口
 */
public interface RepeatingEventRepository {
    /**
     * 保存重复事件
     * @param repeatingEvent 重复事件对象
     * @return 保存后的重复事件
     */
    Mono<RepeatingEvent> save(RepeatingEvent repeatingEvent);
    
    /**
     * 根据ID获取重复事件
     * @param id 重复事件ID
     * @return 重复事件对象
     */
    Mono<RepeatingEvent> findById(String id);
    
    /**
     * 获取所有重复事件
     * @return 重复事件列表
     */
    Flux<RepeatingEvent> findAll();
    
    /**
     * 根据ID删除重复事件
     * @param id 重复事件ID
     * @return 删除结果
     */
    Mono<Void> deleteById(String id);
    
    /**
     * 根据日期获取应该生成的重复事件ID
     * @param listId 日期ID，格式如：20251217
     * @return 重复事件ID集合
     */
    Set<String> getRepeatingEventIdsByListId(String listId);
}
