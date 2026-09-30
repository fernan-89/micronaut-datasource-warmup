package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Db2WarmupPropertiesTest {

    @Test
    void defaultsAreEnabledWithTenAttempts() {
        Db2WarmupProperties properties = new Db2WarmupProperties();

        assertTrue(properties.isEnabled());
        assertTrue(properties.getMaxAttempts() > 0);
    }
}
