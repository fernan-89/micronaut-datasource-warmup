package io.github.fernan89.warmup.mongo;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoWarmupPropertiesTest {

    @Test
    void defaultsAreSensibleAndSettersWork() {
        MongoWarmupProperties properties = new MongoWarmupProperties();

        assertTrue(properties.isEnabled());
        assertEquals(10, properties.getMaxAttempts());
        assertEquals(Duration.ofSeconds(5), properties.getInitialBackoff());
        assertEquals(Duration.ofSeconds(60), properties.getMaxBackoff());
        assertEquals(Duration.ofSeconds(5), properties.getTimeout());
        assertTrue(properties.isFailFast());

        properties.setEnabled(false);
        properties.setMaxAttempts(3);
        properties.setInitialBackoff(Duration.ofSeconds(1));
        properties.setMaxBackoff(Duration.ofSeconds(10));
        properties.setTimeout(Duration.ofSeconds(2));
        properties.setFailFast(false);

        assertFalse(properties.isEnabled());
        assertEquals(3, properties.getMaxAttempts());
        assertEquals(Duration.ofSeconds(1), properties.getInitialBackoff());
        assertEquals(Duration.ofSeconds(10), properties.getMaxBackoff());
        assertEquals(Duration.ofSeconds(2), properties.getTimeout());
        assertFalse(properties.isFailFast());
    }
}
