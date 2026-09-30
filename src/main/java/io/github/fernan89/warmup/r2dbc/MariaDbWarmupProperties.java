package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.mariadb.*} - applies to any R2DBC {@code ConnectionFactory} bean whose driver metadata identifies as MariaDB. */
@ConfigurationProperties("warmup.mariadb")
public class MariaDbWarmupProperties extends AbstractWarmupProperties {
}
