package org.wargamer2010.signshop.configuration.database.clauses;

public enum SQLComparisonType {
    EQUAL("="),
    GREATER(">"),
    LESS("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    NOT_EQUAL("<>"),
    BETWEEN("BETWEEN"),
    LIKE("LIKE"),
    IN("IN"),
    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");

    private final String sql;

    public String sql() {
        return sql;
    }

    SQLComparisonType(String sql) {
        this.sql = sql;
    }
}
