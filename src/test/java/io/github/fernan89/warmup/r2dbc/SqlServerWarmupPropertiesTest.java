package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlServerWarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        SqlServerWarmupProperties properties = new SqlServerWarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
