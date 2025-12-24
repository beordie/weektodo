package beordie.cn.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TodoTest {
    private Todo.Time time;

    @BeforeEach
    public void setUp() {
        time = new Todo.Time();
    }

    @Test
    public void testCalculateDurationMillis_NormalCase() {
        // 用户提供的测试用例：08:00到09:00
        time.setStart("08: 00");
        time.setEnd("09: 00");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：1小时 = 3,600,000毫秒
        assertEquals(3600000L, duration);
    }

    @Test
    public void testCalculateDurationMillis_WithSpaceInTime() {
        // 测试带空格的时间格式（注意方法注释中提到格式是"HH: mm"）
        time.setStart("08: 00");
        time.setEnd("09: 00");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：1小时 = 3,600,000毫秒
        assertEquals(3600000L, duration);
    }

    @Test
    public void testCalculateDurationMillis_ShortDuration() {
        // 测试短时间间隔
        time.setStart("08: 00");
        time.setEnd("08: 05");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：5分钟 = 300,000毫秒
        assertEquals(300000L, duration);
    }

    @Test
    public void testCalculateDurationMillis_StartNull() {
        // 测试开始时间为空
        time.setStart(null);
        time.setEnd("09:00");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：0
        assertEquals(0L, duration);
    }

    @Test
    public void testCalculateDurationMillis_EndNull() {
        // 测试结束时间为空
        time.setStart("08:00");
        time.setEnd(null);
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：0
        assertEquals(0L, duration);
    }

    @Test
    public void testCalculateDurationMillis_InvalidTimeFormat() {
        // 测试无效的时间格式
        time.setStart("08:00:00"); // 带秒的格式
        time.setEnd("09:00:00");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：0（格式错误）
        assertEquals(0L, duration);
    }

    @Test
    public void testCalculateDurationMillis_StartAfterEnd() {
        // 测试开始时间晚于结束时间
        time.setStart("09:00");
        time.setEnd("08:00");
        
        long duration = time.calculateDurationMillis();
        
        // 预期结果：0（开始时间晚于结束时间）
        assertEquals(0L, duration);
    }
}
