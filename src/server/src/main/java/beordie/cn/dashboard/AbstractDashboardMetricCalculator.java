package beordie.cn.dashboard;

import beordie.cn.handler.ConfigHandler;
import beordie.cn.handler.ConfigHandler.DashboardMetricConfig;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractDashboardMetricCalculator implements DashboardMetricCalculator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDashboardMetricCalculator.class);
    
    private final ConfigHandler configHandler;

    public AbstractDashboardMetricCalculator(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    @NonNull
    protected DashboardMetricConfig getCfg() {
        Map<String, DashboardMetricConfig> map = configHandler.getDashboardMetrics();
        if (map == null) {
            logger.info("No dashboard metrics config found, returning default for metric id: {}", id());
            return new DashboardMetricConfig();
        }
        DashboardMetricConfig config = map.get(id());
        if (config == null) {
            logger.info("No config found for metric id: {}, returning default", id());
            return new DashboardMetricConfig();
        }
        logger.info("Retrieved config for metric id: {}, scope: {}", id(), config.getScope());
        return config;
    }

    @Override
    public boolean supports(DashboardScope scope) {
        DashboardScope configScope = getCfg().getScope();
        // 如果配置的scope为ALL，则支持所有请求的scope
        boolean isSupported = configScope == DashboardScope.ALL || scope == configScope;
        logger.info("Metric id: {}, requested scope: {}, config scope: {}, isSupported: {}", 
                   id(), scope, configScope, isSupported);
        return isSupported;
    }
}
