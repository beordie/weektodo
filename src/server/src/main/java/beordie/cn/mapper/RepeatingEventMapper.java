package beordie.cn.mapper;

import beordie.cn.model.RepeatingEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 重复事件数据库映射接口
 */
@Mapper
public interface RepeatingEventMapper extends BaseMapper<RepeatingEvent> {
}
