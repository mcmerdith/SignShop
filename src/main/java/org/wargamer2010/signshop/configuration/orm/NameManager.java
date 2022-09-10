package org.wargamer2010.signshop.configuration.orm;

public class NameManager {
    public interface Strategy {
        String applyForColumn(String columnName);

        String applyForTable(String tableName);
    }

    private static NameManager _instance;

    public static NameManager instance() {
        if (_instance == null) _instance = new NameManager();
        return _instance;
    }

    private NameManager() {
        // Do not instantiate this class
    }

    /*
    Apply a mapping for column and table names
     */

    private Strategy currentStrategy = new Strategy() {
        @Override
        public String applyForColumn(String columnName) {
            return columnName;
        }

        @Override
        public String applyForTable(String tableName) {
            return tableName;
        }
    };

    public String applyStrategiesForColumn(String columnName) {
        return currentStrategy.applyForColumn(columnName);
    }

    public String applyStrategiesForTable(String tableName) {
        return currentStrategy.applyForTable(tableName);
    }

    public void setStrategy(Strategy strategy) {
        if (strategy != null) currentStrategy = strategy;
    }
}
