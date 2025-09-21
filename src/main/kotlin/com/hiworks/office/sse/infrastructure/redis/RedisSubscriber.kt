package com.hiworks.office.sse.infrastructure.redis

import com.hiworks.office.sse.application.SseConnectionManager
import com.hiworks.office.sse.config.redis.RedisChannelProperties
import com.hiworks.office.sse.domain.NotificationEvent
import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

@Component
class RedisSubscriber(
    val reactiveRedisTemplate: ReactiveRedisTemplate<String, RedisEventModel>,
    val sseConnectionManager: SseConnectionManager,
    val redisChannelProperties: RedisChannelProperties
) {
    @PostConstruct
    fun init() {
        reactiveRedisTemplate.listenTo(ChannelTopic.of(redisChannelProperties.logout))
            .map { it.message }
            .subscribeOn(Schedulers.parallel())
            .subscribe { message ->
                sseConnectionManager.send(
                    "test",
                    NotificationEvent(
                        message.eventType,
                        message.officeNo,
                        message.userNo,
                        message.message,
                        message.params
                    )
                )
            }
    }
}