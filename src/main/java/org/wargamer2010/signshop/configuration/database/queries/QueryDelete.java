package org.wargamer2010.signshop.configuration.database.queries;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.SQLHelper;
import org.wargamer2010.signshop.configuration.database.clauses.SQLWhereClause;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class QueryDelete implements DatabaseQuery, DatabaseUpdate {
    private SQLWhereClause whereClause = null;

    // SAFETY NET
    private boolean allowFullTableChanges = false;

    public QueryDelete() { this(null); }

    public QueryDelete(SQLWhereClause whereClause) {
        if (whereClause != null) this.whereClause = whereClause;
    }

    public void allowFullTableChanges() {
        allowFullTableChanges = true;
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String table) throws SQLException {
        if (whereClause == null && !allowFullTableChanges) {
            SignShop.log(String.format("SQL: Dangerous query blocked! DELETE would affect the entire table '%s' and the caller does not allow that", table), Level.SEVERE);
            return null;
        } else {
            SignShop.log(String.format("SQL: Dangerous query permitted, DELETE will affect entire table '%s'", table), Level.WARNING);
        }

        String command = String.format("DELETE FROM %s %s;", table, (whereClause == null) ? "" : whereClause.toSQL());

        SignShop.debugMessage("SQL: " + command);

        PreparedStatement statement = connection.prepareStatement(command);

        SQLHelper.replaceValuesIntoStatement(statement, whereClause.values());

        return null;
    }
}
