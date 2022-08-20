package org.wargamer2010.signshop.configuration.database.queries;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.SQLHelper;
import org.wargamer2010.signshop.configuration.database.clauses.SQLClause;
import org.wargamer2010.signshop.configuration.database.clauses.SQLLikeClause;
import org.wargamer2010.signshop.configuration.database.clauses.SQLOrderByClause;
import org.wargamer2010.signshop.configuration.database.clauses.SQLWhereClause;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuerySelect implements DatabaseQuery {
    private static class SQLJoin {
        private final String table;
        private final String type;
        private final String table1Col;
        private final String table2Col;
        private final List<String> selectedColumns = new ArrayList<>();

        public SQLJoin(String table, String type, String table1Col, String table2Col, List<String> selectedColumns) {
            this.table = table;
            this.type = type;
            this.table1Col = table1Col;
            this.table2Col = table2Col;
            if (selectedColumns != null) this.selectedColumns.addAll(selectedColumns);
        }
    }

    private final List<String> columns = new ArrayList<>();
    private SQLWhereClause whereClause = null;
    private SQLLikeClause likeClause = null;
    private SQLOrderByClause orderByClause = null;

    // Misc SQL functions
    private boolean distinct = false;
    private int limit = -1;

    // Special Column Data
    private String columnDataColumn = null;

    private boolean useMinMax = false;
    private boolean minMaxIsMax = false;

    private boolean count = false;
    private boolean avg = false;
    private boolean sum = false;

    // Joins
    private final List<SQLJoin> joins = new ArrayList<>();

    /**
     * SELECT * FROM table
     */
    public QuerySelect() {
        this(null);
    }

    /**
     * SELECT columns FROM table
     *
     * @param columns A list of column names to select
     */
    public QuerySelect(List<String> columns) {
        if (columns != null) this.columns.addAll(columns);
    }

    /**
     * Special column data
     * <br><strong>Set only one flag. First flag set takes precedence</strong>
     * <br>SQL: <code>SELECT [MIN/MAX/COUNT/AVG/SUM](column)</code>
     *
     * @param column The column to select
     * @param min    Find the minimum row
     * @param max    Find the maximum row
     * @param count  Number of rows in the column
     * @param avg    Find the average value of a numeric column
     * @param sum    Find the sum of a numeric column
     */
    public QuerySelect(String column, boolean min, boolean max, boolean count, boolean avg, boolean sum) {
        columnDataColumn = column;
        if (min || max) {
            this.useMinMax = true;
            this.minMaxIsMax = max;
        } else if (count) {
            this.count = true;
        } else if (avg) {
            this.avg = avg;
        } else if (sum) {
            this.sum = sum;
        }
    }

    public void where(SQLWhereClause whereClause) {
        if (whereClause != null) this.whereClause = whereClause;
    }

    public void like(SQLLikeClause likeClause) {
        if (likeClause != null) this.likeClause = likeClause;
    }

    public void orderBy(SQLOrderByClause orderByClause) {
        if (orderByClause != null) this.orderByClause = orderByClause;
    }

    public void distinct() {
        this.distinct = true;
    }

    public void limit(int limit) {
        if (limit > 0) this.limit = limit;
    }

    public void joinTo(String table2, String joinType, String table1Column, String table2Column, List<String> selectedColumns) {
        if (table2 == null || joinType == null || table1Column == null || table2Column == null) return;

        joins.add(new SQLJoin(table2, joinType, table1Column, table2Column, selectedColumns));
    }

    public List<String> columns(String table) {
        List<String> queryColumns = new ArrayList<>();

        if (joins.isEmpty()) {
            queryColumns.addAll(columns);
        } else {
            // If there are joins, we need to mark all the selected fields with table1
            queryColumns.addAll(columns.stream().map((column) -> String.format("%s.%s", table, column)).collect(Collectors.toList()));

            // Add all the selected fields from the join
            joins.forEach(
                    (join) -> queryColumns.addAll(
                            join.selectedColumns.stream().map(
                                    (column) -> String.format("%s.%s", join.table, column)
                            ).collect(Collectors.toList())));
        }

        // If we aren't selecting anything, select everything
        if (queryColumns.isEmpty()) queryColumns.add("*");

        return queryColumns;
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String table) throws SQLException {
        List<String> queryColumns = columns(table);

        String columDataSelector = null;
        if (useMinMax) {
            columDataSelector = (minMaxIsMax) ? "MAX" : "MIN";
        } else if (count) {
            columDataSelector = "COUNT";
        } else if (avg) {
            columDataSelector = "AVG";
        } else if (sum) {
            columDataSelector = "SUM";
        }

        SQLClause selectorClause = (whereClause != null) ? whereClause : (likeClause != null) ? likeClause : null;

        String likeClauseOrNon = (whereClause == null && likeClause != null) ? likeClause.toSQL() : "";

        String command = String.format("SELECT %s FROM %s %s %s %s %s;",
                (columDataSelector != null) ? String.format("%s(%s)", columDataSelector, columnDataColumn) : String.join(",", queryColumns),
                table,
                (selectorClause != null) ? selectorClause.toSQL() : "",
                (orderByClause != null) ? orderByClause.toSQL() : "",
                (limit > 0) ? limit : "",
                (joins.isEmpty()) ? "" : joins.stream().map(
                        (join) -> String.format("%s JOIN %s ON %s.%s = %s.%s", join.type, join.table, table, join.table1Col, join.table, join.table2Col)
                ).collect(Collectors.joining(" "))
        );

        SignShop.debugMessage("SQL: " + command);

        PreparedStatement statement = connection.prepareStatement(command);

        if (selectorClause != null) SQLHelper.replaceValuesIntoStatement(statement, selectorClause.values());

        return statement;
    }
}
