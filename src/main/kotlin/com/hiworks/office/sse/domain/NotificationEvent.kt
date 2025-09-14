package com.hiworks.office.sse.domain

data class NotificationEvent(
    val eventType: EventType,
    val officeNo: Long,
    val userNo: Long,
    val message: String,
    val params: Any? = null
)
