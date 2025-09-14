package com.hiworks.office.sse.application

import com.hiworks.office.sse.domain.NotificationEvent
import com.hiworks.office.sse.infrastructure.redis.RedisEventModel
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.get

@Component
class SseConnectionManager {
    private val connections: ConcurrentHashMap<String, Sinks.Many<ServerSentEvent<NotificationEvent>>> = ConcurrentHashMap()

    fun connect(userId: String): Flux<ServerSentEvent<NotificationEvent>> {
        val sink = connections.getOrPut(userId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }

        return sink.asFlux().doOnCancel { finish(userId) }
    }

    fun send(userId: String, msg: NotificationEvent) {
        connections[userId]?.tryEmitNext(
            ServerSentEvent.builder<NotificationEvent>()
                .event("message")
                .data(msg)
                .id(userId)
                .comment(userId)
                .build()
        )
    }

    /**
     * Disconnect from SSE
     */
    fun finish(userId: String?) {
        connections[userId]!!.tryEmitComplete()
        connections.remove(userId)
        println("SSE Notification Cancelled by client: $userId")
    }
}