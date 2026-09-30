package io.github.fernan89.warmup;

import java.time.Duration;

/** Test-only, immutable {@link WarmupProperties} implementation with fast-by-default timings. */
final class FixedWarmupProperties implements WarmupProperties {

    private final boolean enabled;
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final Duration timeout;
    private final boolean failFast;

    private FixedWarmupProperties(boolean enabled, int maxAttempts, Duration initialBackoff, Duration maxBackoff,
            Duration timeout, boolean failFast) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
        this.timeout = timeout;
        this.failFast = failFast;
    }

    static FixedWarmupProperties disabled() {
        return new FixedWarmupProperties(false, 1, Duration.ofMillis(1), Duration.ofMillis(1), Duration.ofSeconds(1), true);
    }

    static FixedWarmupProperties enabled(int maxAttempts, boolean failFast) {
        return new FixedWarmupProperties(true, maxAttempts, Duration.ofMillis(5), Duration.ofMillis(5), Duration.ofSeconds(1), failFast);
    }

    static FixedWarmupProperties withBackoffCap(int maxAttempts, long initialBackoffMs, long maxBackoffMs) {
        return new FixedWarmupProperties(true, maxAttempts, Duration.ofMillis(initialBackoffMs), Duration.ofMillis(maxBackoffMs),
                Duration.ofSeconds(1), true);
    }

    static FixedWarmupProperties withTimeout(Duration timeout) {
        return new FixedWarmupProperties(true, 1, Duration.ofMillis(5), Duration.ofMillis(5), timeout, true);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public Duration getInitialBackoff() {
        return initialBackoff;
    }

    @Override
    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }
}
