package beordie.cn.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TemplateEngineUtil测试类
 */
public class TemplateEngineUtilTest {

    @Test
    public void testFormatWithSingleVariable() {
        // 测试基本的模板格式化
        String template = "${hours}H";
        String result = TemplateEngineUtil.format(template, "hours", 5);
        assertEquals("5H", result);
        
        // 测试复杂模板
        template = "已逾期 ${count} 个";
        result = TemplateEngineUtil.format(template, "count", 10);
        assertEquals("已逾期 10 个", result);
        
        // 测试中文模板
        template = "与上周相比增加 ${diff} 个";
        result = TemplateEngineUtil.format(template, "diff", 3);
        assertEquals("与上周相比增加 3 个", result);
    }
    
    @Test
    public void testFormatWithNullTemplate() {
        // 测试空模板
        String result = TemplateEngineUtil.format(null, "hours", 5);
        assertEquals(null, result);
        
        // 测试空字符串模板
        result = TemplateEngineUtil.format("", "hours", 5);
        assertEquals("", result);
    }
    
    @Test
    public void testFormatWithInvalidTemplate() {
        // 测试无效模板（不包含变量）
        String template = "固定文本";
        String result = TemplateEngineUtil.format(template, "hours", 5);
        assertEquals("固定文本", result);
    }
}
