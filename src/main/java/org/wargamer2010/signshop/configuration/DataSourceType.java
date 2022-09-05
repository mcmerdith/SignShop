package org.wargamer2010.signshop.configuration;

public enum DataSourceType {
    YML("yml", "org.sqlite.SQLiteDataSource"),
    SQLITE("sqlite", "org.sqlite.SQLiteDataSource"),
    MARIADB("mariadb", "org.mariadb.jdbc.MariaDbDataSource"),
    MYSQL("mysql", "com.mysql.cj.jdbc.MysqlDataSource");

    private final String jdbcName;
    private final String dataSourceClass;

    public String getJdbcName() {
        return jdbcName;
    }

    public String getDataSourceClass() {
        return dataSourceClass;
    }

    public static DataSourceType fromConfigName(String configName) {
        for (DataSourceType type : .values()){
            if (type.getJdbcName().equalsIgnoreCase(configName)) {
                return type;
            }
        }

        return null;
    }

    DataSourceType(String jdbcName, String dataSourceClass) {
        this.jdbcName = jdbcName;
        this.dataSourceClass = dataSourceClass;
    }

}
