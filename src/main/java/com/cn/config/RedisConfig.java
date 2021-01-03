package com.cn.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    private Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.sentinel.enable: false}")
    private Boolean sentinelEnable;
    @Value("${spring.redis.sentinel.nodes: 127.0.0.1:6379}")
    private String sentinelNodes;
    @Value("${spring.redis.sentinel.masterName: mymaster}")
    private String sentinelMasterName;

    @Bean
    @ConfigurationProperties(prefix = "spring.redis.pool")
    public JedisPoolConfig getRedisConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }

    /**
     * 配置redis的哨兵
     *
     */
    @Bean
    public RedisSentinelConfiguration getSentinelConfiguration() {
        if (sentinelEnable) {
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();

            Set<RedisNode> redisNodeSet = new HashSet<>();
            String[] ipAndPorts = sentinelNodes.split(",");
            for (String ipAndPort : ipAndPorts) {
                String[] temp = ipAndPort.split(":");
                RedisNode senRedisNode = new RedisNode(temp[0], Integer.valueOf(temp[1]));
            }

            redisSentinelConfiguration.setSentinels(redisNodeSet);
            redisSentinelConfiguration.setMaster(sentinelMasterName);
            return redisSentinelConfiguration;
        }else {
            return null;
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.redis")
    public JedisConnectionFactory getConnectionFactory(){
        RedisSentinelConfiguration sentinelConfiguration = getSentinelConfiguration();
        JedisPoolConfig jedisPoolConfig = getRedisConfig();

        JedisConnectionFactory factory;
        if (sentinelConfiguration != null) {
            factory = new JedisConnectionFactory(sentinelConfiguration, jedisPoolConfig);
            logger.info("JedisConnectionFactory bean with sentinel init success.");
        }else {
            factory = new JedisConnectionFactory(jedisPoolConfig);
            logger.info("JedisConnectionFactory bean init success.");
        }
        return factory;
    }

    @Bean
    public RedisTemplate<?, ?> getRedisTemplate() {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(getConnectionFactory());
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
        CacheManager cacheManager = new RedisCacheManager(redisTemplate);
        return cacheManager;
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

}
