package org.wargamer2010.signshop.configuration.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.wargamer2010.signshop.configuration.SignShopConfig;

/**
 * Instruct Hibernate to prefix all tables (except `signshop_master`) with signshop_[server_name]
 */
public class SignShopNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    /**
     * @return The servers prefix
     */
    private static String getPrefix() {
        return String.format("signshop_%s_", SignShopConfig.getDatabaseServerName());
    }

    /**
     * Apply the prefix to a name
     * @param name The name to prefix
     * @return The name, prefixed according {@link SignShopNamingStrategy}
     */
    private static String prefix(String name) {
        // Don't prefix master, it's local
        if (name.equals("signshop_master")) return name;

        return getPrefix() + name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        return context.getIdentifierHelper().toIdentifier(prefix(logicalName.getText()));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment context) {
        return context.getIdentifierHelper().toIdentifier(prefix(logicalName.getText()));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment context) {
        // Don't prefix the columns
        return logicalName;
    }
}
