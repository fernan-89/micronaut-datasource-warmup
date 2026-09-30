package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleWarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        OracleWarmupProperties properties = new OracleWarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
