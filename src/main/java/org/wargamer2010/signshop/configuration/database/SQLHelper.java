package org.wargamer2010.signshop.configuration.database;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.clauses.SQLField;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SQLHelper {
    public static class WrappedStatement {
        private String statement;
        private List<Object> values;

        public WrappedStatement(String statement, List<Object> values) {
            this.statement = statement;
            this.values = values;
        }

        public String getStatement() { return statement; }
        public List<Object> getValues() { return values; }

    }
    @SafeVarargs
    public static void replaceValuesIntoStatement(PreparedStatement statement, Collection<Object>... valueSet) {
        AtomicInteger currentIndex = new AtomicInteger();

        SignShop.debugMessage(Arrays.stream(valueSet).map((collection) -> collection.stream().map(Object::toString).collect(Collectors.joining(","))).collect(Collectors.joining(",")));

        for (Collection<Object> values : valueSet) {
            values.forEach((value) -> {
                try {
                    statement.setObject(currentIndex.incrementAndGet(), value);
                } catch (SQLException e) {
                    DatabaseDataSource.sqlError(e);
                }
            });
        }
    }

    public static WrappedStatement prepareTableFields(List<SQLField> fields) {
        StringBuilder statement = new StringBuilder();

        List<String> externalConstraints = new ArrayList<>();
        List<Object> constraintValues = new ArrayList<>();

        fields.forEach((field) -> {
            statement.append(field.toSQL()).append(",");
            externalConstraints.addAll(field.constraints());
            constraintValues.addAll(field.values());
        });

        externalConstraints.forEach((constraint) -> {
            statement.append(constraint).append(",");
        });

        return new WrappedStatement(statement.toString(), constraintValues);
    }

    public static String prepareValue(Object value) {
        if (value instanceof String) {
            return String.format("'%s'", value);
        } else if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * Convert a List of row values to a List of SQL maps containing the data.
     * <br>Each row value will be mapped to the provided column
     * <br>A seller_id Foreign Key will be inserted into each map
     * @param seller_id The seller_id Foreign Key
     * @param column The column for the row values
     * @param rowValues The values of the rows
     * @return
     */
    public static List<Map<String, Object>> rowsWithSellerIdForeignKey(int seller_id, String column, List<Object> rowValues) {
        List<Map<String, Object>> rows = new ArrayList<>();

        rowValues.forEach((rowValue) -> {
            if (rowValue == null) return;
            Map<String, Object> row = new HashMap<>();

            row.put("seller_id", seller_id);
            row.put(column, rowValue);

            rows.add(row);
        });

        return rows;
    }

//    public static Object readBlob(ResultSet results) {
//        sqlBlobToObject()
//    }
//
//    public static Object sqlBlobToObject(byte[] buffer) {
//
//    }
}
