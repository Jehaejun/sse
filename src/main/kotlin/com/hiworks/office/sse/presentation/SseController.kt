package com.hiworks.office.sse.presentation

import com.hiworks.office.sse.application.NotificationService
import com.hiworks.office.sse.application.SseConnectionManager
import com.hiworks.office.sse.domain.NotificationEvent
import com.hiworks.office.sse.presentation.request.SseMessageModel
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class SseController(
    private val sseConnectionManager: SseConnectionManager,
    private val notificationService: NotificationService
) {
    @GetMapping("/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): Flux<ServerSentEvent<NotificationEvent>> {
        return sseConnectionManager.connect("test")
    }

    @PostMapping("/publish")
    fun publish(
        @RequestBody model: SseMessageModel
    ): Mono<Long> {
        return notificationService.send(
            NotificationEvent(
                model.eventType,
                1994L,
                123L,
                model.message,
                model.params
            )
        )
    }
}
