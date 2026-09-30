package io.github.fernan89.warmup.r2dbc;

import io.github.fernan89.warmup.WarmupProperties;

import java.util.Locale;

/**
 * Maps an R2DBC driver's own {@code ConnectionFactoryMetadata.getName()} to the
 * {@code @ConfigurationProperties} class that governs its warmup. Covers the relational engines most
 * common in production (PostgreSQL, MySQL, Oracle, Microsoft SQL Server, MariaDB, IBM Db2) - adding
 * another one is a new enum constant, nothing else changes, since every engine speaks the same
 * vendor-agnostic {@code io.r2dbc.spi} API (no per-driver dependency, no observer subclass).
 */
enum VendorMapping {

    MYSQL(MySqlWarmupProperties.class, "mysql"),
    POSTGRES(PostgresWarmupProperties.class, "postgres"),
    ORACLE(OracleWarmupProperties.class, "oracle"),
    SQL_SERVER(SqlServerWarmupProperties.class, "sql server", "mssql", "sqlserver"),
    MARIADB(MariaDbWarmupProperties.class, "mariadb"),
    DB2(Db2WarmupProperties.class, "db2");

    private final Class<? extends WarmupProperties> propertiesType;
    private final String[] keywords;

    VendorMapping(Class<? extends WarmupProperties> propertiesType, String... keywords) {
        this.propertiesType = propertiesType;
        this.keywords = keywords;
    }

    Class<? extends WarmupProperties> propertiesType() {
        return propertiesType;
    }

    private boolean matches(String normalizedVendorName) {
        for (String keyword : keywords) {
            if (normalizedVendorName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /** @return the matching mapping for this driver's vendor name, or {@code null} if none is recognised. */
    static VendorMapping resolve(String vendorName) {
        String normalized = vendorName.toLowerCase(Locale.ROOT);
        for (VendorMapping mapping : values()) {
            if (mapping.matches(normalized)) {
                return mapping;
            }
        }
        return null;
    }
}
