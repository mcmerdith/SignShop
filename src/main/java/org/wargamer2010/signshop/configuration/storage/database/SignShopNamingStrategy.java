package org.wargamer2010.signshop.configuration.storage.database;

import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.orm.NameManager;

/**
 * Instruct Hibernate to prefix all tables (except `signshop_master`) with signshop_[server_name]
 */
public class SignShopNamingStrategy implements NameManager.Strategy {
    /**
     * @return The servers prefix
     */
    private static String getPrefix() {
        return String.format("signshop_%s_", SignShopConfig.getDatabaseServerName());
    }

    /**
     * Apply the prefix to a name
     *
     * @param name The name to prefix
     * @return The name, prefixed according {@link SignShopNamingStrategy}
     */
    private static String prefix(String name) {
        // Don't prefix master, it's local
        if (name.equals("signshop_master")) return name;

        return getPrefix() + name;
    }

    @Override
    public String applyForColumn(String columnName) {
        // don't prefix columns
        return columnName;
    }

    @Override
    public String applyForTable(String tableName) {
        return prefix(tableName);
    }
}
