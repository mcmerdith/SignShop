package org.wargamer2010.signshop.configuration.database.clauses;

public enum SQLConstraintType {
    /**
     * Field cannot be null.
     * <br>[MODIFY ]column type NOT NULL
     * <br>Does not accept any values
     */
    NOT_NULL("NOT NULL", true),

    /**
     * Field must be unique.
     * <br>[ADD/DROP ]CONSTRAINT UC_name[ UNIQUE (column)]
     * <br>Accepts an optional comma seperated String of other columns to include in the UNIQUE (col)
     */
    UNIQUE("UNIQUE", false),

    /**
     * Field is the primary key of the table
     * <br>[ADD/DROP ]PRIMARY KEY (column)
     * <br>Accepts an optional comma seperated String of other columns to include in the PRIMARY KEY (col)
     */
    PRIMARY_KEY("PRIMARY KEY", false),

    /**
     * Field is a foreign key reference.
     * <br>[ADD/DROP ]CONSTRAINT FK_name[ FOREIGN KEY (column) REFERENCES table(column)]
     * <br>Accepts a {@link SQLForeignKey}
     */
    FOREIGN_KEY("FOREIGN KEY", false),

    /**
     * A check clause. Field must pass the {@link SQLWhereClause}
     * <br>[ADD ]CONSTRAINT CHK_name CHECK (whereClause)
     * <br>DROP CHECK CHK_name
     * <br>Accepts a {@link SQLWhereClause }
     */
    CHECK("CHECK", false),

    /**
     * Default value for column
     * <br>[SET/DROP ]DEFAULT
     * <br>Accepts any {@link Object}
     */
    DEFAULT("DEFAULT", true),

    /**
     * Auto increment this column
     * AUTO_INCREMENT
     * Does not accept any values
     */
    AUTO_INCREMENT("AUTO_INCREMENT", true);

    private final String sql;
    private final boolean inline;

    public String sql() {
        return sql;
    }

    public boolean isInline() {
        return inline;
    }

    SQLConstraintType(String sql, boolean inline) {
        this.sql = sql;
        this.inline = inline;
    }
}
