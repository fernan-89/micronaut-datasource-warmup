package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MariaDbWarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        MariaDbWarmupProperties properties = new MariaDbWarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
