package org.wargamer2010.signshop.configuration.database.queries;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DatabaseQuery {
    /**
     * Return a prepared SQL statement
     * @param connection A database connection
     * @param table The name of the table to query
     * @return The SQL statement
     * @throws SQLException if there is an issue connecting to the database
     */
    @Nullable
    PreparedStatement prepareStatement(Connection connection, String table) throws SQLException;
}
