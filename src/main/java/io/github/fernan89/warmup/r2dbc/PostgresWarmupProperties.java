package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.postgres.*} - applies to any R2DBC {@code ConnectionFactory} bean whose driver metadata identifies as PostgreSQL. */
@ConfigurationProperties("warmup.postgres")
public class PostgresWarmupProperties extends AbstractWarmupProperties {
}
