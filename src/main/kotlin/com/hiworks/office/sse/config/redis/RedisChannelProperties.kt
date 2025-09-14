package com.hiworks.office.sse.config.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "hiworks.redis.channels")
data class RedisChannelProperties(
    val channels: Map<String, String> = mutableMapOf(),
    val logout: String = ""
)
