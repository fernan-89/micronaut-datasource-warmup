package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.WarmupProperties;
import io.github.fernan89.warmup.WarmupRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * One instance per R2DBC {@code ConnectionFactory} bean (including multiple named datasources), created
 * automatically by {@code @EachBean} - no separate observer class per relational engine, since every
 * engine {@link VendorMapping} recognises speaks the same {@code io.r2dbc.spi} API and a plain
 * {@code SELECT 1} works on all of them. The connection factory's own driver metadata decides which
 * {@code warmup.<engine>.*} configuration applies; a vendor this library does not recognise is skipped
 * rather than guessed at.
 */
@EachBean(ConnectionFactory.class)
@Requires(notEnv = "test")
@Slf4j
public class R2dbcWarmupObserver implements ApplicationEventListener<StartupEvent> {

    private final ConnectionFactory connectionFactory;
    private final WarmupRunner runner;
    private final ApplicationContext applicationContext;
    private final String vendor;
    private final WarmupProperties properties;

    @Inject
    public R2dbcWarmupObserver(ConnectionFactory connectionFactory, WarmupRunner runner, ApplicationContext applicationContext) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory must not be null");
        this.runner = Objects.requireNonNull(runner, "runner must not be null");
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext must not be null");
        this.vendor = connectionFactory.getMetadata().getName();
        this.properties = resolveProperties(vendor, applicationContext);
    }

    private static WarmupProperties resolveProperties(String vendor, ApplicationContext applicationContext) {
        VendorMapping mapping = VendorMapping.resolve(vendor);
        return mapping == null ? null : applicationContext.getBean(mapping.propertiesType());
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        if (properties == null) {
            log.debug("[WARMUP] r2dbc: no matching configuration for vendor '{}', skipping.", vendor);
            return;
        }
        runner.run(vendor, properties, applicationContext, this::probe);
    }

    private Mono<Void> probe() {
        return Mono.usingWhen(
                Mono.from(connectionFactory.create()),
                connection -> Mono.from(connection.createStatement("SELECT 1").execute())
                        .flatMap(result -> Mono.from(result.map((row, metadata) -> true))),
                connection -> Mono.from(connection.close()),
                (connection, error) -> Mono.from(connection.close()),
                connection -> Mono.from(connection.close())
        ).then();
    }
}
