package com.hiworks.office.sse.application

import com.hiworks.office.sse.application.port.EventPublishPort
import com.hiworks.office.sse.domain.NotificationEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NotificationService(
    private val eventPublishPort: EventPublishPort
) {
    fun send(message: NotificationEvent): Mono<Long> {
        return eventPublishPort.publish(message)
    }
}