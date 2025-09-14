package com.hiworks.office.sse.application.port

import com.hiworks.office.sse.domain.NotificationEvent
import reactor.core.publisher.Mono

interface EventPublishPort {
    fun publish(message: NotificationEvent): Mono<Long>
}