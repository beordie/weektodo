package beordie.cn.config;

import beordie.cn.handler.ConfigHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * 配置信息路由配置
 * 定义配置信息的API路由
 */
@Configuration
public class ConfigRouterConfig {

    @Autowired
    private ConfigHandler configHandler;

    @Bean
    public RouterFunction<ServerResponse> configRoutes() {
        return RouterFunctions
                // 获取所有配置信息
                .route(GET("/api/config")
                        .and(accept(MediaType.APPLICATION_JSON)), configHandler::getAllConfigs)
                // 获取分类配置
                .andRoute(GET("/api/config/categories")
                        .and(accept(MediaType.APPLICATION_JSON)), configHandler::getCategories)
                // 获取优先级配置
                .andRoute(GET("/api/config/priorities")
                        .and(accept(MediaType.APPLICATION_JSON)), configHandler::getPriorities);
    }
}
