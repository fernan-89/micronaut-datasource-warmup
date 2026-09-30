package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.mysql.*} - applies to any R2DBC {@code ConnectionFactory} bean whose driver metadata identifies as MySQL. */
@ConfigurationProperties("warmup.mysql")
public class MySqlWarmupProperties extends AbstractWarmupProperties {
}
