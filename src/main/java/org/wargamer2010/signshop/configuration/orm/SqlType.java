package org.wargamer2010.signshop.configuration.orm;

import java.sql.Types;

public enum SqlType {
    /**
     * SQL Type: `INTEGER`
     */
    INTEGER("INTEGER", SQLite.INTEGER),
    /**
     * SQL Type: `CHARACTER`, `VARCHAR`, `TEXT`, `CLOB`
     */
    STRING("VARCHAR", SQLite.TEXT),
    /**
     * SQL Type: `BLOB`
     */
    BINARY("BLOB", SQLite.BLOB),
    /**
     * SQL Type: `REAL`, `DOUBLE`, `FLOAT`
     */
    DOUBLE("FLOAT", SQLite.REAL),
    /**
     * SQL Type: `BIT`
     */
    BOOLEAN("BIT", SQLite.NUMERIC);

    private final String value;
    private final SQLite sqlite;

    public String getValue() {
        return this.value;
    }

    public String getSqLite() {
        return this.sqlite.name();
    }

    SqlType(String value, SQLite sqlite) {
        this.value = value;
        this.sqlite = sqlite;
    }

    public enum SQLite {
        INTEGER,
        TEXT,
        BLOB,
        REAL,
        NUMERIC
    }
}
