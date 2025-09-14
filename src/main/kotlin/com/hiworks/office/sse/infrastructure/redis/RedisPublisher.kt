package com.hiworks.office.sse.infrastructure.redis

import com.hiworks.office.sse.application.port.EventPublishPort
import com.hiworks.office.sse.config.redis.RedisChannelProperties
import com.hiworks.office.sse.domain.EventType
import com.hiworks.office.sse.domain.NotificationEvent
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisPublisher(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, RedisEventModel>,
    private val redisChannelProperties: RedisChannelProperties
) : EventPublishPort {
    override fun publish(
        message: NotificationEvent
    ): Mono<Long> {
        return reactiveRedisTemplate.convertAndSend(
            getChannelName(message.eventType),
            RedisEventModel(
                message.eventType,
                message.officeNo,
                message.userNo,
                message.message,
                message.params
            )
        )
    }

    private fun getChannelName(eventType: EventType): String {
        return when (eventType) {
            EventType.CONCURRENT_LOGOUT -> redisChannelProperties.logout
        }
    }
}