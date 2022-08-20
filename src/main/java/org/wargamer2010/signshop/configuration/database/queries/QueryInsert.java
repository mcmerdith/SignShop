package org.wargamer2010.signshop.configuration.database.queries;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class QueryInsert implements DatabaseQuery, DatabaseUpdate {
    private Map<String, Object> data = new HashMap<>();

    public QueryInsert(Map<String, Object> data) {
        // Load the data
        data.forEach((column, value) -> {
            if (column == null) {
                SignShop.log("SQL: Invalid column (null), ignoring", Level.WARNING);
                return;
            }

            this.data.put(column, value);
        });
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String table) throws SQLException {
        if (data.isEmpty()) {
            SignShop.log(String.format("SQL: Invalid INSERT query on %s! Query will affect 0 rows", table), Level.WARNING);
            return null;
        }

        String command = String.format("INSERT INTO %s (%s) VALUES (%s);",
                table,
                String.join(",", data.keySet()),
                String.join(",",Collections.nCopies(data.size(), "?"))
        );

        SignShop.debugMessage("SQL: " + command);

        PreparedStatement statement = connection.prepareStatement(command);

        SQLHelper.replaceValuesIntoStatement(statement, data.values());

        return statement;
    }
}
