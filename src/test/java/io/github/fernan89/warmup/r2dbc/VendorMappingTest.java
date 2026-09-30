package io.github.fernan89.warmup.r2dbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VendorMappingTest {

    @Test
    void recognisesMySql() {
        assertEquals(VendorMapping.MYSQL, VendorMapping.resolve("MySQL"));
    }

    @Test
    void recognisesPostgres() {
        assertEquals(VendorMapping.POSTGRES, VendorMapping.resolve("PostgreSQL"));
    }

    @Test
    void recognisesOracle() {
        assertEquals(VendorMapping.ORACLE, VendorMapping.resolve("Oracle Database"));
    }

    @Test
    void recognisesSqlServerByItsFullName() {
        assertEquals(VendorMapping.SQL_SERVER, VendorMapping.resolve("Microsoft SQL Server"));
    }

    @Test
    void recognisesSqlServerByItsShortDriverName() {
        assertEquals(VendorMapping.SQL_SERVER, VendorMapping.resolve("r2dbc-mssql"));
    }

    @Test
    void recognisesSqlServerByASingleWordVendorName() {
        assertEquals(VendorMapping.SQL_SERVER, VendorMapping.resolve("sqlserver"));
    }

    @Test
    void recognisesMariaDb() {
        assertEquals(VendorMapping.MARIADB, VendorMapping.resolve("MariaDB"));
    }

    @Test
    void recognisesDb2() {
        assertEquals(VendorMapping.DB2, VendorMapping.resolve("IBM Db2"));
    }

    @Test
    void anUnrecognisedVendorResolvesToNull() {
        assertNull(VendorMapping.resolve("SQLite"));
    }

    @Test
    void propertiesTypeExposesTheConfigurationClass() {
        assertEquals(MySqlWarmupProperties.class, VendorMapping.MYSQL.propertiesType());
    }
}
