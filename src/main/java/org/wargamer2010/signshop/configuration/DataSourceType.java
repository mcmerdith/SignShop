package org.wargamer2010.signshop.configuration;

import org.wargamer2010.signshop.util.SignShopLogger;

public enum DataSourceType {
    YML("yml", false, "org.sqlite.SQLiteDataSource"),
    SQLITE("sqlite", false, "org.sqlite.SQLiteDataSource"),
    MARIADB("mariadb", true, "org.mariadb.jdbc.MariaDbDataSource"),
    MYSQL("mysql", true, "com.mysql.cj.jdbc.MysqlDataSource");

    private final String jdbcName;
    private final boolean external;
    private final String dataSourceClass;

    public String getJdbcName() {
        return jdbcName;
    }

    public boolean isExternal() {
        return external;
    }

    public String getDataSourceClass() {
        return dataSourceClass;
    }

    public static DataSourceType fromConfigName(String configName) {
        for (DataSourceType type : DataSourceType.values()) {
            if (type.getJdbcName().equalsIgnoreCase(configName)) {
                return type;
            }
        }

        SignShopLogger.getConfigLogger().error(configName + " is not a valid DataSourceType");

        return null;
    }

    DataSourceType(String jdbcName, boolean external, String dataSourceClass) {
        this.jdbcName = jdbcName;
        this.external = external;
        this.dataSourceClass = dataSourceClass;
    }

}
