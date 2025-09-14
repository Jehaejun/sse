package com.hiworks.office.sse.infrastructure.redis

import com.hiworks.office.sse.domain.EventType

data class RedisEventModel(
    val eventType: EventType,
    val officeNo: Long,
    val userNo: Long,
    val message: String,
    val params: Any? = null
)