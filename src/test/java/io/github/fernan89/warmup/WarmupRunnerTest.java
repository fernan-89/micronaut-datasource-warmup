package io.github.fernan89.warmup;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class WarmupRunnerTest {

    private final WarmupRunner runner = new WarmupRunner();

    @Test
    void disabledSkipsTheProbeEntirely() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        AtomicInteger calls = new AtomicInteger();
        Supplier<Mono<Void>> probe = () -> { calls.incrementAndGet(); return Mono.empty(); };

        runner.run("mongo", FixedWarmupProperties.disabled(), ctx, probe);

        assertEquals(0, calls.get());
        verify(ctx, never()).stop();
    }

    @Test
    void succeedsImmediatelyWithoutTouchingTheApplicationContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Supplier<Mono<Void>> probe = Mono::empty;

        assertDoesNotThrow(() -> runner.run("mongo", FixedWarmupProperties.enabled(5, true), ctx, probe));

        verify(ctx, never()).stop();
    }

    @Test
    void retriesOnFailureAndEventuallySucceeds() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        AtomicInteger attempts = new AtomicInteger();
        Supplier<Mono<Void>> probe = () -> Mono.defer(() ->
                attempts.getAndIncrement() < 1 ? Mono.error(new RuntimeException("not ready yet")) : Mono.empty());

        runner.run("mysql", FixedWarmupProperties.enabled(5, true), ctx, probe);

        assertEquals(2, attempts.get());
        verify(ctx, never()).stop();
    }

    @Test
    void singleAttemptExhaustsImmediatelyAndStopsTheContextWhenFailFast() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Supplier<Mono<Void>> probe = () -> Mono.error(new RuntimeException("down"));

        assertDoesNotThrow(() -> runner.run("postgres", FixedWarmupProperties.enabled(1, true), ctx, probe));

        verify(ctx).stop();
    }

    @Test
    void exhaustionWithoutFailFastLogsButNeverStopsTheContext() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Supplier<Mono<Void>> probe = () -> Mono.error(new RuntimeException("down"));

        assertDoesNotThrow(() -> runner.run("postgres", FixedWarmupProperties.enabled(2, false), ctx, probe));

        verify(ctx, never()).stop();
    }

    @Test
    void backoffIsCappedOnceItWouldExceedTheConfiguredMaximum() {
        // initialBackoff=10ms doubles to 20ms on the second retry, which exceeds maxBackoff=15ms:
        // exercises both the uncapped (first retry) and capped (second retry) branch of the backoff formula.
        ApplicationContext ctx = mock(ApplicationContext.class);
        Supplier<Mono<Void>> probe = () -> Mono.error(new RuntimeException("down"));

        assertDoesNotThrow(() -> runner.run("mysql", FixedWarmupProperties.withBackoffCap(3, 10, 15), ctx, probe));

        verify(ctx).stop();
    }

    @Test
    void aProbeThatNeverCompletesIsTreatedAsAFailedAttempt() {
        ApplicationContext ctx = mock(ApplicationContext.class);
        Supplier<Mono<Void>> probe = () -> Mono.never();

        assertDoesNotThrow(() -> runner.run("mongo", FixedWarmupProperties.withTimeout(Duration.ofMillis(20)), ctx, probe));

        verify(ctx).stop();
    }
}
