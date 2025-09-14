package com.hiworks.office.sse.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.hiworks.office.sse.infrastructure.redis.RedisEventModel
import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.MapPropertySource
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.time.temporal.ChronoUnit


@Configuration
class RedisConfig {

    /*    @Bean
        @Primary*/
    fun reactiveRedisConnectionFactory_(): ReactiveRedisConnectionFactory {
        // 클러스터 호스트 세팅
        // 1. Redis 클러스터 속성을 Map으로 정의
        val clusterProperties = mapOf<String, Any>(
            "spring.redis.cluster.nodes" to "localhost:6379",
            "spring.redis.cluster.max-redirects" to 3
            // "spring.redis.password" to "your-password" //필요시 암호 설정
        )

        // 2. Map을 사용하여 PropertySource 생성
        val propertySource = MapPropertySource("redisClusterConfig", clusterProperties)

        // 3. PropertySource를 사용하여 RedisClusterConfiguration 인스턴스 생성
        val clusterConfiguration = RedisClusterConfiguration.of(propertySource)


        // topology 자동 업데이트 옵션 추가
        val clusterTopologyRefreshOptions: ClusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enableAllAdaptiveRefreshTriggers() // MOVED, ASK, PERSISTENT_RECONNECTS, UNCOVERED_SLOT, UNKOWN_NODE trigger시 refresh 진행
            .enablePeriodicRefresh(Duration.ofHours(1L)) //1시간 마다 해당 Refresh 설정 사용
            .build()


        //clientOption 추가
        val clientOptions: ClientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(clusterTopologyRefreshOptions)
            .build()


        val clientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.of(3, ChronoUnit.SECONDS))
            .clientOptions(clientOptions)
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build()

        return LettuceConnectionFactory(clusterConfiguration, clientConfiguration)
    }

    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(
        clientResources: ClientResources
    ): ReactiveRedisConnectionFactory {
        val clientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.of(3, ChronoUnit.SECONDS))
            .shutdownTimeout(Duration.ofMillis(100))
            .clientResources(clientResources)
            .readFrom(ReadFrom.MASTER_PREFERRED) // 단일 서버라면 MASTER_PREFERRED
            .build()

        val redisConfig = RedisStandaloneConfiguration(
            "localhost", 6379
        )

        return LettuceConnectionFactory(redisConfig, clientConfiguration)
    }

    @Bean
    fun reactiveRedisTemplate(
        factory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, RedisEventModel> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, RedisEventModel::class.java)

        val serializationContext = RedisSerializationContext.newSerializationContext<String, RedisEventModel>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean(destroyMethod = "shutdown")
    fun clientResources(): ClientResources {
        return DefaultClientResources.builder()
            .ioThreadPoolSize(4)  // I/O 스레드 풀 크기
            .computationThreadPoolSize(4) // 계산 스레드 풀 크기
            .build()
    }

}