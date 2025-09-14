package com.hiworks.office.sse.config.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer


@Configuration
class RedisListenerConfig {
    @Bean
    fun reactiveRedisMessageListenerContainer(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisMessageListenerContainer {
        return ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory)
    }
}