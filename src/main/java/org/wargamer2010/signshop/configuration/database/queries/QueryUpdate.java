package org.wargamer2010.signshop.configuration.database.queries;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.SQLHelper;
import org.wargamer2010.signshop.configuration.database.clauses.SQLWhereClause;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class QueryUpdate implements DatabaseQuery, DatabaseUpdate {
    private final Map<String, Object> data = new HashMap<>();
    private SQLWhereClause whereClause = null;

    // SAFETY NET
    private boolean allowFullTableChanges = false;

    public QueryUpdate(Map<String, Object> data) {
        this(data, null);
    }

    public QueryUpdate(Map<String, Object> data, SQLWhereClause whereClause) {
        if (data != null) {
            data.forEach((column, value) -> {
                if (column == null) {
                    SignShop.log("SQL: Invalid column (null), ignoring", Level.WARNING);
                    return;
                }

                this.data.put(column, value);
            });
        }

        if (whereClause != null) this.whereClause = whereClause;
    }

    public void allowFullTableChanges() {
        allowFullTableChanges = true;
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String table) throws SQLException {
        List<String> preparedData = new ArrayList<>();

        data.keySet().forEach((key) -> preparedData.add(String.format("%s = ?", key)));

        if (whereClause == null && !allowFullTableChanges) {
            SignShop.log(String.format("SQL: Dangerous query blocked! UPDATE would affect the entire table '%s' and the caller does not allow that", table), Level.SEVERE);
            return null;
        } else {
            SignShop.log(String.format("SQL: Dangerous query permitted, UPDATE will affect entire table '%s'", table), Level.WARNING);
        }

        String command = String.format("UPDATE %s SET %s %s;", table, String.join(",", preparedData), (whereClause == null) ? "" : whereClause.toSQL());

        SignShop.debugMessage("SQL: " + command);

        PreparedStatement statement = connection.prepareStatement(command);

        SQLHelper.replaceValuesIntoStatement(statement, data.values(), whereClause.values());

        return statement;
    }
}
