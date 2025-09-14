package com.hiworks.office.sse.presentation.request

import com.hiworks.office.sse.domain.EventType

data class SseMessageModel(
    val eventType: EventType,
    val message: String,
    val params: Any? = null
)
