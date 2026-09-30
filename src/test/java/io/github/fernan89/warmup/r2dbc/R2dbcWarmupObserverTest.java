package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.WarmupRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.StartupEvent;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class R2dbcWarmupObserverTest {

    private ConnectionFactory connectionFactory;
    private ConnectionFactoryMetadata metadata;
    private Connection connection;
    private Statement statement;
    private Result result;
    private ApplicationContext applicationContext;
    private StartupEvent event;
    private MySqlWarmupProperties mysqlProperties;
    private final WarmupRunner runner = new WarmupRunner();

    @BeforeEach
    void setUp() {
        connectionFactory = mock(ConnectionFactory.class);
        metadata = mock(ConnectionFactoryMetadata.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        result = mock(Result.class);
        applicationContext = mock(ApplicationContext.class);
        event = mock(StartupEvent.class);
        when(connectionFactory.getMetadata()).thenReturn(metadata);
        doReturn(Mono.just(connection)).when(connectionFactory).create();
        when(connection.createStatement("SELECT 1")).thenReturn(statement);
        when(connection.close()).thenReturn(Mono.empty());
        doReturn(Mono.just(result)).when(statement).execute();
        doReturn(Mono.just(true)).when(result).map(any(java.util.function.BiFunction.class));

        mysqlProperties = new MySqlWarmupProperties();
        mysqlProperties.setMaxAttempts(2);
        mysqlProperties.setInitialBackoff(Duration.ofMillis(1));
        mysqlProperties.setMaxBackoff(Duration.ofMillis(1));
        when(applicationContext.getBean(MySqlWarmupProperties.class)).thenReturn(mysqlProperties);
    }

    @Test
    @DisplayName("a recognised vendor resolves its configuration bean and runs a successful SELECT 1")
    void recognisedVendorSucceeds() {
        when(metadata.getName()).thenReturn("MySQL");

        new R2dbcWarmupObserver(connectionFactory, runner, applicationContext).onApplicationEvent(event);

        verify(statement).execute();
        verify(connection).close();
        verify(applicationContext, never()).stop();
    }

    @Test
    @DisplayName("an unrecognised vendor is skipped entirely - no connection or bean lookup is ever attempted")
    void unrecognisedVendorIsSkipped() {
        when(metadata.getName()).thenReturn("SQLite");

        new R2dbcWarmupObserver(connectionFactory, runner, applicationContext).onApplicationEvent(event);

        verify(connectionFactory, never()).create();
        verify(applicationContext, never()).getBean(any(Class.class));
        verify(applicationContext, never()).stop();
    }

    @Test
    @DisplayName("every retry is exhausted on a failing probe, then the context is stopped")
    void failingProbeStopsTheContext() {
        when(metadata.getName()).thenReturn("MySQL");
        doReturn(Mono.error(new IllegalStateException("connection refused"))).when(connectionFactory).create();

        new R2dbcWarmupObserver(connectionFactory, runner, applicationContext).onApplicationEvent(event);

        verify(applicationContext).stop();
    }

    @Test
    @DisplayName("a connection acquired successfully but a failing SELECT 1 still closes the connection")
    void executeFailureAfterAcquiredConnectionClosesIt() {
        when(metadata.getName()).thenReturn("MySQL");
        doReturn(Mono.error(new IllegalStateException("syntax error"))).when(statement).execute();

        new R2dbcWarmupObserver(connectionFactory, runner, applicationContext).onApplicationEvent(event);

        verify(connection, atLeastOnce()).close();
        verify(applicationContext).stop();
    }

    @Test
    @DisplayName("a probe that never completes is cancelled by the timeout and the connection is still closed")
    void timeoutCancelsAHangingProbeAndStillClosesTheConnection() {
        when(metadata.getName()).thenReturn("MySQL");
        mysqlProperties.setTimeout(Duration.ofMillis(20));
        doReturn(Mono.never()).when(statement).execute();

        new R2dbcWarmupObserver(connectionFactory, runner, applicationContext).onApplicationEvent(event);

        verify(connection, atLeastOnce()).close();
        verify(applicationContext).stop();
    }

    @Test
    @DisplayName("mandatory collaborators are null-checked")
    void nullGuards() {
        assertThrows(NullPointerException.class, () -> new R2dbcWarmupObserver(null, runner, applicationContext));
        assertThrows(NullPointerException.class, () -> new R2dbcWarmupObserver(connectionFactory, null, applicationContext));
        assertThrows(NullPointerException.class, () -> new R2dbcWarmupObserver(connectionFactory, runner, null));
    }
}
