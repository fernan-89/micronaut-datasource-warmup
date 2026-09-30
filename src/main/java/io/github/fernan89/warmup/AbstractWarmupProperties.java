package io.github.fernan89.warmup;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * Shared field set for every datasource's {@code @ConfigurationProperties} class - see the individual
 * getters on {@link WarmupProperties} for what each one controls. Sensible defaults mean a consumer
 * only ever has to set {@code enabled} (and, occasionally, {@code max-attempts}); everything else is
 * tuned already.
 */
@Getter
@Setter
public abstract class AbstractWarmupProperties implements WarmupProperties {

    private boolean enabled = true;
    private int maxAttempts = 10;
    private Duration initialBackoff = Duration.ofSeconds(5);
    private Duration maxBackoff = Duration.ofSeconds(60);
    private Duration timeout = Duration.ofSeconds(5);
    private boolean failFast = true;
}
