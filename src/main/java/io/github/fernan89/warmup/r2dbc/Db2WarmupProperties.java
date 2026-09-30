package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.db2.*} - applies to any R2DBC {@code ConnectionFactory} bean whose driver metadata identifies as IBM Db2. */
@ConfigurationProperties("warmup.db2")
public class Db2WarmupProperties extends AbstractWarmupProperties {
}
