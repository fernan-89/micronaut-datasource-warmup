package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.AbstractWarmupProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/** {@code warmup.sqlserver.*} - applies to any R2DBC {@code ConnectionFactory} bean whose driver metadata identifies as Microsoft SQL Server. */
@ConfigurationProperties("warmup.sqlserver")
public class SqlServerWarmupProperties extends AbstractWarmupProperties {
}
