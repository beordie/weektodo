package beordie.cn.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 缓存配置类，支持Redis或内存缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 内存缓存管理器
     * @return ConcurrentMapCacheManager
     */
    @Bean
    @Primary
    public CacheManager memoryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 全局配置
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }

    /**
     * Redis缓存管理器配置
     * 如果需要使用Redis，取消注释以下代码并添加Redis依赖
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisCacheManager
     */
    /*
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24)) // 设置缓存过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("todoTimeCache", config.entryTtl(Duration.ofDays(7))) // 为特定缓存设置不同过期时间
                .build();
    }
    */
}