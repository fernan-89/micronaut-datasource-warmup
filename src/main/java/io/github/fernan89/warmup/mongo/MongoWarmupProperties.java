package io.github.fernan89.warmup.mongo;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.mongo.*} - active only when a reactive {@code MongoClient} bean is present. */
@ConfigurationProperties("warmup.mongo")
public class MongoWarmupProperties extends AbstractWarmupProperties {
}
