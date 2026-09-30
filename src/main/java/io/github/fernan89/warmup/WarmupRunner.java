package io.github.fernan89.warmup;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * The shared retry/backoff/fail-fast engine every datasource-specific warmup observer delegates to.
 * Runs {@code probe} on the calling (startup) thread, synchronously, retrying with exponential backoff
 * (capped at {@link WarmupProperties#getMaxBackoff()}) up to {@link WarmupProperties#getMaxAttempts()}
 * times. On final exhaustion, stops the {@code ApplicationContext} when
 * {@link WarmupProperties#isFailFast()} is {@code true} - so an orchestrator's readiness probe never
 * reports ready against a datasource that never came up - or simply logs and lets the application
 * continue starting when it is {@code false}.
 */
@Singleton
@Slf4j
public class WarmupRunner {

    public void run(String component, WarmupProperties props, ApplicationContext applicationContext, Supplier<Mono<Void>> probe) {
        if (!props.isEnabled()) {
            log.debug("[WARMUP] {} warmup is disabled, skipping.", component);
            return;
        }

        int maxRetries = Math.max(0, props.getMaxAttempts() - 1);
        long startTime = System.currentTimeMillis();

        probe.get()
                .timeout(props.getTimeout())
                .retryWhen(Retry.from(signals -> signals.flatMap(signal -> {
                    long attempt = signal.totalRetries();
                    if (attempt < maxRetries) {
                        Duration delay = backoff(props, attempt);
                        log.warn("[WARMUP] {} attempt {}/{} failed, retrying in {}s... [error={}]",
                                component, attempt + 1, props.getMaxAttempts(), delay.toSeconds(), signal.failure().getMessage());
                        return Mono.delay(delay);
                    }
                    log.error("[WARMUP] {} exhausted all {} attempt(s).", component, props.getMaxAttempts());
                    return Mono.error(signal.failure());
                })))
                .doOnSuccess(v -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[WARMUP] {} is UP. [latency={}ms]", component, duration);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("[WARMUP] {} is DOWN after {}ms: {}", component, duration, error.getMessage());
                    if (props.isFailFast()) {
                        applicationContext.stop();
                    }
                })
                .onErrorResume(error -> Mono.empty())
                .block();
    }

    private Duration backoff(WarmupProperties props, long attempt) {
        long millis = props.getInitialBackoff().toMillis() * (1L << attempt);
        Duration delay = Duration.ofMillis(millis);
        return delay.compareTo(props.getMaxBackoff()) > 0 ? props.getMaxBackoff() : delay;
    }
}
