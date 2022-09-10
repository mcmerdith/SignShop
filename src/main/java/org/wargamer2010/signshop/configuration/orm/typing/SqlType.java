package org.wargamer2010.signshop.configuration.orm.typing;

public enum SqlType {
    INT(null, SQLite.INTEGER, true),
    TEXT(null, SQLite.TEXT, true),
    /**
     * SQL Type: `VARCHAR`
     */
    STRING("VARCHAR", SQLite.TEXT, false),
    BLOB(null, SQLite.BLOB, true),
    FLOAT(null, SQLite.REAL, false),
    /**
     * SQL Type: `BIT`
     */
    BOOLEAN("BIT", SQLite.NUMERIC, false),
    DATE(null, SQLite.REAL, false),
    DATETIME(null, SQLite.REAL, false),
    TIMESTAMP(null, SQLite.REAL, false),
    /**
     * Auto detect SQL DDL
     */
    AUTO(null, null, false);

    private final String value;
    private final SQLite sqlite;
    /**
     * This is not the same as `length`
     * Sizeable indicates if the type accepts a prefix: (TINY/MEDIUM/LONG)
     */
    private final boolean sizeable;

    public String getValue() {
        return getValue(false, null);
    }

    public String getValue(Size size) {
        return getValue(false, size);
    }

    public String getValue(boolean sqlite, Size size) {
        if (sqlite) {
            return getSqlite();
        } else {
            String base = (this.value == null) ? this.name() : this.value;
            if (!this.sizeable || size == null || size == Size.NONE) return base;
            return String.format("%s%s", size.name(), base);
        }
    }

    public String getSqlite() {
        return this.sqlite.name();
    }

    SqlType(String value, SQLite sqlite, boolean sizeable) {
        this.value = value;
        this.sqlite = sqlite;
        this.sizeable = sizeable;
    }

    /**
     * Define the available SQLite types
     */
    private enum SQLite {
        INTEGER,
        TEXT,
        BLOB,
        REAL,
        NUMERIC
    }

    public enum Size {
        TINY,
        MEDIUM,
        LONG,
        NONE
    }
}
