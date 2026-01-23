package beordie.cn.handler;

import beordie.cn.dashboard.DashboardScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 配置信息处理器
 * 用于返回从application.yml读取的配置信息
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class ConfigHandler {

    /**
     * 分类对象类
     */
    public static class Category {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 优先级对象类
     */
    public static class Priority {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // 从application.yml中读取分类配置
    private List<Category> categories;

    // 从application.yml中读取优先级配置
    private List<Priority> priorities;

    // 从application.yml中读取任务时间配置
    private TaskTimeConfig taskTimeConfig;

    /**
     * 获取分类配置
     * @return 分类配置列表
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * 设置分类配置
     * @param categories 分类配置列表
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    /**
     * 获取任务时间配置
     * @return 任务时间配置
     */
    public TaskTimeConfig getTaskTimeConfig() {
        return taskTimeConfig;
    }

    /**
     * 设置任务时间配置
     * @param taskTimeConfig 任务时间配置
     */
    public void setTaskTimeConfig(TaskTimeConfig taskTimeConfig) {
        this.taskTimeConfig = taskTimeConfig;
    }

    /**
     * 获取优先级配置
     * @return 优先级配置列表
     */
    public List<Priority> getPriorities() {
        return priorities;
    }

    /**
     * 设置优先级配置
     * @param priorities 优先级配置列表
     */
    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    /**
     * 任务时间配置内部类
     */
    public static class TaskTimeConfig {
        // 逾期时间阈值（天）
        private int overdueThresholdDays;
        // 即将到期阈值（天）
        private int upcomingThresholdDays;
        // 逾期事项阈值（秒）
        private int overdueThresholdSeconds;

        /**
         * 获取逾期事项阈值（秒）
         * @return 逾期事项阈值（秒）
         */
        public int getOverdueThresholdSeconds() {
            return overdueThresholdSeconds;
        }

        /**
         * 设置逾期事项阈值（秒）
         * @param overdueThresholdSeconds 逾期事项阈值（秒）
         */
        public void setOverdueThresholdSeconds(int overdueThresholdSeconds) {
            this.overdueThresholdSeconds = overdueThresholdSeconds;
        }

        /**
         * 获取逾期时间阈值
         * @return 逾期时间阈值（天）
         */
        public int getOverdueThresholdDays() {
            return overdueThresholdDays;
        }

        /**
         * 设置逾期时间阈值
         * @param overdueThresholdDays 逾期时间阈值（天）
         */
        public void setOverdueThresholdDays(int overdueThresholdDays) {
            this.overdueThresholdDays = overdueThresholdDays;
        }

        /**
         * 获取即将到期阈值
         * @return 即将到期阈值（天）
         */
        public int getUpcomingThresholdDays() {
            return upcomingThresholdDays;
        }

        /**
         * 设置即将到期阈值
         * @param upcomingThresholdDays 即将到期阈值（天）
         */
        public void setUpcomingThresholdDays(int upcomingThresholdDays) {
            this.upcomingThresholdDays = upcomingThresholdDays;
        }
    }

    public static class DashboardFooterConfig {
        private String type;
        private String template;
        private String color;
        private String icon;
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    public static class DashboardMetricConfig {
        private String title;
        private Integer order;
        private DashboardScope scope;
        private List<DashboardFooterConfig> footers;

        public DashboardFooterConfig getFooter(String type) {
            return this.getFooters().stream().filter(footer -> footer != null && footer.getType() != null && footer.getType().equals(type)).findFirst().orElse(null);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public DashboardScope getScope() {
            return scope;
        }

        public void setScope(DashboardScope scope) {
            this.scope = scope;
        }

        public List<DashboardFooterConfig> getFooters() {
            return footers;
        }

        public void setFooters(List<DashboardFooterConfig> footers) {
            this.footers = footers;
        }
    }

    public static class DashboardConfig {
        private Map<String, DashboardMetricConfig> metrics;

        public Map<String, DashboardMetricConfig> getMetrics() {
            return metrics;
        }

        public void setMetrics(Map<String, DashboardMetricConfig> metrics) {
            this.metrics = metrics;
        }
    }

    private DashboardConfig dashboard;

    public Map<String, DashboardMetricConfig> getDashboardMetrics() {
        return dashboard != null ? dashboard.getMetrics() : null;
    }

    public DashboardConfig getDashboard() {
        return dashboard;
    }

    public void setDashboard(DashboardConfig dashboard) {
        this.dashboard = dashboard;
    }

    /**
     * 获取所有配置信息
     */
    public Mono<ServerResponse> getAllConfigs(ServerRequest request) {
        Map<String, Object> configs = Map.of(
                "categories", categories,
                "priorities", priorities,
                "taskTimeConfig", taskTimeConfig
        );
        return ServerResponse.ok()
                .bodyValue(configs);
    }

    /**
     * 获取分类配置
     */
    public Mono<ServerResponse> getCategories(ServerRequest request) {
        // 如果从配置文件中读取的分类为空，则返回默认分类列表
        List<Category> categoryList;
        if (this.categories != null && !this.categories.isEmpty()) {
            categoryList = this.categories;
        } else {
            // 创建默认分类列表
            Category work = new Category();
            work.setId("work");
            work.setName("工作");
            
            Category study = new Category();
            study.setId("study");
            study.setName("学习");
            
            Category life = new Category();
            life.setId("life");
            life.setName("生活");
            
            Category fitness = new Category();
            fitness.setId("fitness");
            fitness.setName("健身");
            
            Category entertainment = new Category();
            entertainment.setId("entertainment");
            entertainment.setName("娱乐");
            
            Category other = new Category();
            other.setId("other");
            other.setName("其他");
            
            categoryList = List.of(work, study, life, fitness, entertainment, other);
        }
        return ServerResponse.ok()
                .bodyValue(Map.of("categories", categoryList));
    }

    /**
     * 获取优先级配置
     */
    public Mono<ServerResponse> getPriorities(ServerRequest request) {
        // 如果从配置文件中读取的优先级为空，则返回默认优先级列表
        List<Priority> priorityList;
        if (this.priorities != null && !this.priorities.isEmpty()) {
            priorityList = this.priorities;
        } else {
            // 创建默认优先级列表
            Priority low = new Priority();
            low.setId(0);
            low.setName("低");
            
            Priority medium = new Priority();
            medium.setId(1);
            medium.setName("中");
            
            Priority high = new Priority();
            high.setId(2);
            high.setName("高");
            
            priorityList = List.of(low, medium, high);
        }
        return ServerResponse.ok()
                .bodyValue(Map.of("priorities", priorityList));
    }
}
