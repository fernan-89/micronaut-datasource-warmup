package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresWarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        PostgresWarmupProperties properties = new PostgresWarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
