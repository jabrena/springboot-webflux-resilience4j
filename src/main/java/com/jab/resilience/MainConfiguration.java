package com.jab.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class MainConfiguration {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {

        //https://resilience4j.readme.io/docs/circuitbreaker
        //https://github.com/resilience4j/resilience4j/blob/master/resilience4j-circuitbreaker/src/main/java/io/github/resilience4j/circuitbreaker/CircuitBreakerConfig.java
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            //.slidingWindowType(COUNT_BASED)
            //.slidingWindowType(TIME_BASED)
            //.minimumNumberOfCalls(2)
            .failureRateThreshold(50)
            //.slowCallRateThreshold(100)
            //.slowCallDurationThreshold(Duration.ofMillis(200))
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(2)
            //.permittedNumberOfCallsInHalfOpenState(2)
            //.slidingWindow()
            //.automaticTransitionFromOpenToHalfOpenEnabled(true)
            //.recordExceptions()
            //.recordException()
            //.ignoreExceptions()
            .build();
        return circuitBreakerConfig;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public TimeLimiterConfig timeLimiterConfig() {

        //https://resilience4j.readme.io/docs/timeout
        //https://github.com/resilience4j/resilience4j/blob/master/resilience4j-timelimiter/src/main/java/io/github/resilience4j/timelimiter/TimeLimiterConfig.java
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            //.cancelRunningFuture()
            .build();
    }

    /*
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer(
            CircuitBreakerConfig circuitBreakerConfig,
            TimeLimiterConfig timeLimiterConfig,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build());
            factory.configureCircuitBreakerRegistry(circuitBreakerRegistry);
        };
    }
    */

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer2(
            CircuitBreakerConfig circuitBreakerConfig,
            TimeLimiterConfig timeLimiterConfig,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(timeLimiterConfig)
                    .circuitBreakerConfig(circuitBreakerConfig)
                    .build());
            factory.configureCircuitBreakerRegistry(circuitBreakerRegistry);
        };
    }
}
