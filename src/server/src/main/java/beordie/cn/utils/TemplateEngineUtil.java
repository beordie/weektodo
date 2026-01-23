package beordie.cn.utils;

/**
 * 模板引擎工具类
 * 用于字符串模板格式化，支持${variable}语法
 */
public class TemplateEngineUtil {

    /**
     * 使用模板引擎格式化字符串
     * @param template 模板字符串，例如："与上周相比增加 ${diff} 个"
     * @param variableName 变量名
     * @param variableValue 变量值
     * @return 格式化后的字符串
     */
    public static String format(String template, String variableName, Object variableValue) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        try {
            String placeholder = "${" + variableName + "}";
            String valueStr;
            
            // 如果是数字类型，进行格式化
            if (variableValue instanceof Number) {
                Number number = (Number) variableValue;
                // 如果是浮点数，保留两位小数
                if (variableValue instanceof Double || variableValue instanceof Float) {
                    valueStr = String.format("%.2f", number.doubleValue());
                } else {
                    // 整数直接转换
                    valueStr = String.valueOf(number.longValue());
                }
            } else {
                // 非数字类型直接转换
                valueStr = String.valueOf(variableValue);
            }
            
            return template.replace(placeholder, valueStr);
        } catch (Exception e) {
            // 如果模板解析失败，返回原始模板
            return template;
        }
    }

    /**
     * 使用模板引擎格式化字符串，支持多个变量
     * @param template 模板字符串
     * @param variables 变量映射，键为变量名，值为变量值
     * @return 格式化后的字符串
     */
    public static String format(String template, java.util.Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        try {
            String result = template;
            if (variables != null && !variables.isEmpty()) {
                for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    Object value = entry.getValue();
                    String valueStr;
                    
                    // 如果是数字类型，进行格式化
                    if (value instanceof Number) {
                        Number number = (Number) value;
                        // 如果是浮点数，保留两位小数
                        if (value instanceof Double || value instanceof Float) {
                            valueStr = String.format("%.2f", number.doubleValue());
                        } else {
                            // 整数直接转换
                            valueStr = String.valueOf(number.longValue());
                        }
                    } else {
                        // 非数字类型直接转换
                        valueStr = String.valueOf(value);
                    }
                    
                    result = result.replace(placeholder, valueStr);
                }
            }
            return result;
        } catch (Exception e) {
            // 如果模板解析失败，返回原始模板
            return template;
        }
    }

    /**
     * 检查模板字符串是否有效
     * @param template 模板字符串
     * @return 是否有效
     */
    public static boolean isValidTemplate(String template) {
        return template != null;
    }
}
