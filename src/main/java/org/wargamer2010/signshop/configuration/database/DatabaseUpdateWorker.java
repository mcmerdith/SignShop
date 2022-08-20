package org.wargamer2010.signshop.configuration.database;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.database.queries.DatabaseQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class DatabaseUpdateWorker extends TimerTask {
    public static class DatabaseCommand {

        public final String tableName;
        public final DatabaseQuery databaseQuery;
        public final Consumer<Integer> callback;

        public DatabaseCommand(String tableName, DatabaseQuery databaseQuery) {
            this(tableName, databaseQuery, null);
        }

        public DatabaseCommand(String tableName, DatabaseQuery databaseQuery, Consumer<Integer> callback) {
            this.tableName = tableName;
            this.databaseQuery = databaseQuery;
            this.callback = callback;
        }

        public int execute(Connection connection) {
            if (tableName == null || databaseQuery == null) {
                if (callback != null) callback.accept(-1);
                return -1;
            }

            try (PreparedStatement statement = databaseQuery.prepareStatement(connection, tableName)) {
                int updated = statement.executeUpdate();
                if (callback != null) callback.accept(updated);
                return updated;
            } catch (SQLException e) {
                DatabaseDataSource.sqlError(e);
                if (callback != null) callback.accept(-1);
                return -1;
            }
        }

        public PreparedStatement executeAndReturnStatement(Connection connection) {
            if (tableName == null || databaseQuery == null) {
                if (callback != null) callback.accept(-1);
                return null;
            }

            try {
                PreparedStatement statement = databaseQuery.prepareStatement(connection, tableName);

                int updated = statement.executeUpdate();
                if (callback != null) callback.accept(updated);

                return statement;
            } catch (SQLException e) {
                DatabaseDataSource.sqlError(e);
                if (callback != null) callback.accept(-1);

                return null;
            }
        }
    }

    public static class DatabaseTransaction {
        private final Queue<Function<PreparedStatement, DatabaseCommand>> commandProviders = new ConcurrentLinkedQueue<>();
        private final boolean cascadeStatements;

        /**
         * Create a group of SQL commands to be executed in a transaction
         *
         * @param first                The first command
         * @param cascadeStatements    Pass each Command provider: false= The result of the first command; true= The result of the previous command
         * @param nextCommandProviders Functions that accept a PreparedStatement and return a database command
         */
        @SafeVarargs
        public DatabaseTransaction(DatabaseCommand first, boolean cascadeStatements, Function<PreparedStatement, DatabaseCommand>... nextCommandProviders) {
            this.cascadeStatements = cascadeStatements;
            this.commandProviders.add((s) -> first);
            this.commandProviders.addAll(List.of(nextCommandProviders));
        }

        public DatabaseTransaction addCommandProvider(Function<PreparedStatement, DatabaseCommand> nextCommandProvider) {
            this.commandProviders.add(nextCommandProvider);
            return this;
        }

        public DatabaseTransaction addCommandProviders(Collection<Function<PreparedStatement, DatabaseCommand>> nextCommandProviders) {
            this.commandProviders.addAll(nextCommandProviders);
            return this;
        }

        /**
         * Execute all commands in a transaction on the database. If any command fails the transaction will be rolled back
         *
         * @param connection A fresh database connection
         * @return If the transaction succeeded
         * @throws SQLException
         */
        public boolean execute(Connection connection) {
            boolean failure = false;

            try (connection) {
                connection.setAutoCommit(false);

                PreparedStatement previousStatement = null;

                while (!commandProviders.isEmpty()) {
                    // Get the next command
                    DatabaseCommand current = commandProviders.poll().apply(previousStatement);

                    if (current == null) {
                        // Next command failed to generate, abort the transaction
                        failure = true;
                        // Close the previous statement if it existed
                        if (previousStatement != null) previousStatement.close();

                        return false;
                    }

                    // If we have a previous statement and won't be reusing it, close it
                    if (previousStatement != null && cascadeStatements) previousStatement.close();

                    // Run the next command
                    PreparedStatement currentStatement = current.executeAndReturnStatement(connection);

                    if (currentStatement == null) {
                        // Command failed to execute
                        failure = true;
                        // Close the previous statement if it existed
                        if (previousStatement != null) previousStatement.close();

                        return false;
                    }

                    // If there is no previous statement, or we are cascading statements, update the current statement
                    if (previousStatement == null || cascadeStatements) previousStatement = currentStatement;
                }

                // Clean it up
                if (previousStatement != null) previousStatement.close();

                return true;
            } catch (SQLException e) {
                DatabaseDataSource.sqlError(e);
                return false;
            } finally {
                try {
                    if (failure) {
                        connection.rollback();
                    } else {
                        connection.commit();
                    }
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    // Oh well, database probably wasn't connected anyway
                    DatabaseDataSource.sqlError(e);
                }
            }
        }
    }

    private final Queue<DatabaseCommand> databaseQueue = new ConcurrentLinkedQueue<>();
    private final Queue<DatabaseTransaction> transactionQueue = new ConcurrentLinkedQueue<>();

    public void execute(DatabaseQuery query, String table) {
        databaseQueue.add(new DatabaseCommand(table, query));
    }

    public void transaction(DatabaseTransaction transaction) {
        transactionQueue.add(transaction);
    }

    @Override
    public void run() {
        if (!(Storage.get() instanceof DatabaseDataSource)) {
            SignShop.log("[DatabaseUpdateWorker] Running while the Storage is not a Database. Stopping...", Level.WARNING);
            this.cancel();
            return;
        }

        DatabaseDataSource source = (DatabaseDataSource) Storage.get();

        while (!databaseQueue.isEmpty()) {
            try {
                databaseQueue.poll().executeAndReturnStatement(source.getConnection()).close();
            } catch (SQLException e) {
                DatabaseDataSource.sqlError(e);
            }
        }

        while (!transactionQueue.isEmpty()) {
            try {
                transactionQueue.poll().execute(source.newConnection());
            } catch (SQLException e) {
                DatabaseDataSource.sqlError(e);
            }
        }
    }

    public void dispose() {
        this.cancel();
    }
}
