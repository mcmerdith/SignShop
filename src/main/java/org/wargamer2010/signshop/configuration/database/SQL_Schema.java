package org.wargamer2010.signshop.configuration.database;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.database.clauses.SQLConstraintType;
import org.wargamer2010.signshop.configuration.database.clauses.SQLField;
import org.wargamer2010.signshop.configuration.database.clauses.SQLForeignKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SQL_Schema {
    public static boolean initTables() {
        if (!(Storage.get() instanceof DatabaseDataSource)) return false;
        DatabaseDataSource source = (DatabaseDataSource) Storage.get();

        Connection c = source.getConnection();

        return createTable(c, TABLE_SELLERS.tableName(), TABLE_SELLERS.all(0))
                && createTable(c, TABLE_ACTIVATABLES.tableName(), TABLE_ACTIVATABLES.all(0))
                && createTable(c, TABLE_CONTAINABLES.tableName(), TABLE_CONTAINABLES.all(0))
                && createTable(c, TABLE_ITEMS.tableName(), TABLE_ITEMS.all(0));
    }

    /*
    Table schemas
     */
    private interface SQLTable {
        String columnName();

        SQLField get();
    }

    public enum TABLE_SELLERS implements SQLTable {
        //        ID("id", () -> new SQLField("id", "INT")
//                .constrain(SQLConstraintType.AUTO_INCREMENT)
//                .constrain(SQLConstraintType.PRIMARY_KEY)
//                .constrain(SQLConstraintType.NOT_NULL)),
        SHOPWORLD("shopworld", () -> new SQLField("shopworld", "VARCHAR(255)")
                .constrain(SQLConstraintType.NOT_NULL)),
        OWNER("owner", () -> new SQLField("owner", "VARCHAR(255)")
                .constrain(SQLConstraintType.NOT_NULL)),
        SIGN("sign", () -> new SQLField("sign", "VARCHAR(255)")
                .constrain(SQLConstraintType.NOT_NULL)
                .constrain(SQLConstraintType.PRIMARY_KEY)),
        //                .constrain(SQLConstraintType.UNIQUE)),
        MISC("misc", () -> new SQLField("misc", "VARCHAR(255)"));

        private final String column;
        private final Supplier<SQLField> supplier;

        @Override
        public String columnName() {
            return column;
        }

        @Override
        public SQLField get() {
            return supplier.get();
        }

        public static List<SQLField> all(int command) {
            return Arrays.stream(values()).map((value) -> value.supplier.get().command(command)).collect(Collectors.toList());
        }

        public static List<String> allColumnNames() {
            return Arrays.stream(values()).map(SQLTable::columnName).collect(Collectors.toList());
        }

        public static String tableName() {
            return "sellers";
        }

        TABLE_SELLERS(String column, Supplier<SQLField> supplier) {
            this.column = column;
            this.supplier = supplier;
        }
    }

    public enum TABLE_ACTIVATABLES implements SQLTable {
        ID("id", () -> new SQLField("id", "INT")
                .constrain(SQLConstraintType.AUTO_INCREMENT)
                .constrain(SQLConstraintType.PRIMARY_KEY)),
        SELLER_SIGN("seller_sign", () -> new SQLField("seller_sign", "VARCHAR(255)")
                .constrain(SQLConstraintType.FOREIGN_KEY, new SQLForeignKey("sellers", "sign")
                        .onDelete(SQLForeignKey.Action.CASCADE)
                        .onUpdate(SQLForeignKey.Action.CASCADE))),
        ACTIVATABLE("activatable", () -> new SQLField("activatable", "VARCHAR(255)")
                .constrain(SQLConstraintType.NOT_NULL));

        private final String column;
        private final Supplier<SQLField> supplier;

        @Override
        public String columnName() {
            return column;
        }

        @Override
        public SQLField get() {
            return supplier.get();
        }

        public static List<SQLField> all(int command) {
            return Arrays.stream(values()).map((value) -> value.supplier.get().command(command)).collect(Collectors.toList());
        }

        public static List<String> allColumnNames() {
            return Arrays.stream(values()).map(SQLTable::columnName).collect(Collectors.toList());
        }

        public static String tableName() {
            return "activatables";
        }

        TABLE_ACTIVATABLES(String column, Supplier<SQLField> supplier) {
            this.column = column;
            this.supplier = supplier;
        }
    }

    public enum TABLE_CONTAINABLES implements SQLTable {
        ID("id", () -> new SQLField("id", "INT")
                .constrain(SQLConstraintType.AUTO_INCREMENT)
                .constrain(SQLConstraintType.PRIMARY_KEY)),
        SELLER_SIGN("seller_sign", () -> new SQLField("seller_sign", "VARCHAR(255)")
                .constrain(SQLConstraintType.FOREIGN_KEY, new SQLForeignKey("sellers", "sign")
                        .onDelete(SQLForeignKey.Action.CASCADE)
                        .onUpdate(SQLForeignKey.Action.CASCADE))),
        CONTAINABLE("containable", () -> new SQLField("containable", "VARCHAR(255)")
                .constrain(SQLConstraintType.NOT_NULL));

        private final String column;
        private final Supplier<SQLField> supplier;

        @Override
        public String columnName() {
            return column;
        }

        @Override
        public SQLField get() {
            return supplier.get();
        }

        public static List<SQLField> all(int command) {
            return Arrays.stream(values()).map((value) -> value.supplier.get().command(command)).collect(Collectors.toList());
        }

        public static List<String> allColumnNames() {
            return Arrays.stream(values()).map(SQLTable::columnName).collect(Collectors.toList());
        }

        public static String tableName() {
            return "containables";
        }

        TABLE_CONTAINABLES(String column, Supplier<SQLField> supplier) {
            this.column = column;
            this.supplier = supplier;
        }
    }

    public enum TABLE_ITEMS implements SQLTable {
        ID("id", () -> new SQLField("id", "INT")
                .constrain(SQLConstraintType.AUTO_INCREMENT)
                .constrain(SQLConstraintType.PRIMARY_KEY)),
        SELLER_SIGN("seller_sign", () -> new SQLField("seller_sign", "VARCHAR(255)")
                .constrain(SQLConstraintType.FOREIGN_KEY, new SQLForeignKey("sellers", "sign")
                        .onDelete(SQLForeignKey.Action.CASCADE)
                        .onUpdate(SQLForeignKey.Action.CASCADE))),
        ITEM("item", () -> new SQLField("item", "BLOB")
                .constrain(SQLConstraintType.NOT_NULL));

        private final String column;
        private final Supplier<SQLField> supplier;

        @Override
        public String columnName() {
            return column;
        }

        @Override
        public SQLField get() {
            return supplier.get();
        }

        public static List<SQLField> all(int command) {
            return Arrays.stream(values()).map((value) -> value.supplier.get().command(command)).collect(Collectors.toList());
        }

        public static List<String> allColumnNames() {
            return Arrays.stream(values()).map(SQLTable::columnName).collect(Collectors.toList());
        }

        public static String tableName() {
            return "items";
        }

        TABLE_ITEMS(String column, Supplier<SQLField> supplier) {
            this.column = column;
            this.supplier = supplier;
        }
    }

    /*
    SQL stuff
     */

    // Table functions
    // CREATE TABLE table (columns);
    public static boolean createTable(Connection connection, String table, List<SQLField> columns) {
        List<String> builtColumns = new ArrayList<>();
        List<String> allConstraints = new ArrayList<>();
        List<Object> allValues = new ArrayList<>();

        // Seperate the data
        columns.forEach((column) -> {
            builtColumns.add(column.toSQL());
            allConstraints.addAll(column.constraints());
            allValues.addAll(column.values());
        });

        String columnData = String.join(", ", builtColumns) + ((allConstraints.isEmpty()) ? "" : ", " + String.join(", ", allConstraints));

        String statement = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", table, columnData);

        SignShop.debugMessage(statement);

        try (PreparedStatement preppedStatement = connection.prepareStatement(statement)) {

            SQLHelper.replaceValuesIntoStatement(preppedStatement, allValues);

            preppedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            DatabaseDataSource.sqlError(e);
            return false;
        }
    }

    // DROP TABLE table;
    public static boolean dropTable(Connection connection, String table) {
        if (table == null || table.isBlank()) return false;

        String statement = String.format("DROP TABLE IF EXISTS %s;", table);

        SignShop.debugMessage(statement);

        try (PreparedStatement preppedStatement = connection.prepareStatement(statement)) {
            preppedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            DatabaseDataSource.sqlError(e);
            return false;
        }
    }

    // ALTER TABLE table ADD column newField;
    public static boolean addColumn(Connection connection, String table, String column, SQLField newField) {
        return false;
    }

    // ALTER TABLE table DROP column;
    public static boolean dropColumn(Connection connection, String table, String column) {
        return false;
    }

    // ALTER TABLE table MODIFY COLUMN column newField
    public static boolean alterColumn(Connection connection, String table, SQLField newField) {
        return false;
    }
}
