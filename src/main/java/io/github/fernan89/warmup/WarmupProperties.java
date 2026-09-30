package io.github.fernan89.warmup;

import java.time.Duration;

/**
 * Configuration contract every datasource-specific {@code @ConfigurationProperties} class implements
 * (e.g. {@code warmup.mongo.*}, {@code warmup.mysql.*}, {@code warmup.postgres.*}). {@link WarmupRunner}
 * depends only on this interface, so adding a new datasource means implementing it, not touching the
 * retry engine.
 */
public interface WarmupProperties {

    /** Whether this datasource's warmup observer should run at all. Default {@code true} on every implementation. */
    boolean isEnabled();

    /** How many probe attempts to make before giving up. The first attempt counts as attempt 1. */
    int getMaxAttempts();

    /** Delay before the second attempt; doubles on each subsequent retry, capped at {@link #getMaxBackoff()}. */
    Duration getInitialBackoff();

    /** Upper bound on the exponential backoff delay between attempts. */
    Duration getMaxBackoff();

    /** Per-attempt timeout: how long a single probe may take before it counts as a failed attempt. */
    Duration getTimeout();

    /**
     * When every attempt is exhausted: {@code true} stops the {@code ApplicationContext} (fail-fast, so
     * an orchestrator's readiness probe never reports ready against a datasource that never came up);
     * {@code false} logs the failure and lets the application continue starting.
     */
    boolean isFailFast();
}
