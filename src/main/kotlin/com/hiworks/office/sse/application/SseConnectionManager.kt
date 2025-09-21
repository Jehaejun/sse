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
        val sink = connections.computeIfAbsent(userId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }

        println("연결 요청: $userId" + "conn size: " + connections.size)

        return sink.asFlux().doOnCancel { finish(userId) }
    }

    fun send(userId: String, msg: NotificationEvent) {

        connections.forEach{ (key, value) ->
            val tryEmitNext = value.tryEmitNext(
                ServerSentEvent.builder<NotificationEvent>()
                    .event("message")
                    .data(msg)
                    .id(userId)
                    .comment(userId)
                    .build()
            )

            if (tryEmitNext.isFailure) {
                println("전송 오류 : $key")
            } else {
                println("전송 성공 :  $key" + "conn size: " + connections.size)
            }
        }

/*        connections[userId]?.tryEmitNext(
            ServerSentEvent.builder<NotificationEvent>()
                .event("message")
                .data(msg)
                .id(userId)
                .comment(userId)
                .build()
        )*/
    }

    /**
     * Disconnect from SSE
     */
    fun finish(userId: String?) {
        connections[userId]!!.tryEmitComplete()
        connections.remove(userId)
        println("SSE Notification Cancelled by client: $userId" + " conn size: [${connections.size}]")
    }
}