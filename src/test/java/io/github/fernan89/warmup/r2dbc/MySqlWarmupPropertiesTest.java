package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlWarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        MySqlWarmupProperties properties = new MySqlWarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
