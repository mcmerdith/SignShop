package org.wargamer2010.signshop.configuration.storage.database.util;

import com.zaxxer.hikari.HikariConfig;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.SignShopLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Properties;

public class DatabaseUtil {
    private static final HikariConfig sqliteConfig = new HikariConfig();
    private static final Properties MYSQL_OPTIMIZATIONS = new Properties();

    static {
        sqliteConfig.setDriverClassName("org.sqlite.JDBC");
        try {
            sqliteConfig.setJdbcUrl("jdbc:sqlite:" + new File(SignShop.getInstance().getDataFolder(), "signshop.db").getPath());
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().error("Unable to reserve file 'signshop.db' in the plugin folder!");
            sqliteConfig.setJdbcUrl("jdbc:sqlite:signshop.db");
        }

        MYSQL_OPTIMIZATIONS.put("prepStmtCacheSize", "250");
        MYSQL_OPTIMIZATIONS.put("prepStmtCacheSqlLimit", "2048");
        MYSQL_OPTIMIZATIONS.put("cachePrepStmts", "true");
        MYSQL_OPTIMIZATIONS.put("useServerPrepStmts", "true");
        MYSQL_OPTIMIZATIONS.put("useLocalSessionState", "true");
        MYSQL_OPTIMIZATIONS.put("rewriteBatchedStatements", true);
        MYSQL_OPTIMIZATIONS.put("cacheResultSetMetadata", "true");
        MYSQL_OPTIMIZATIONS.put("cacheServerConfiguration", "true");
        MYSQL_OPTIMIZATIONS.put("elideSetAutoCommits", "true");
        MYSQL_OPTIMIZATIONS.put("maintainTimeStats", String.valueOf(SignShopConfig.debugging()));
    }

    /**
     * Get the configuration for the set database
     *
     * @param onlyConnectionProperties Only return the connection properties
     * @return The configuration
     */
    public static HikariConfig getConfig() {
        return getConfigWithProperties(SignShopConfig.getDataSource(), null);
    }

    /**
     * Get a configuration for a database
     *
     * @param type                     The type of database being configured
     * @param onlyConnectionProperties Only return the connection properties, does nothing if custom properties are set
     * @param customProperties         Custom properties for the databse
     * @return The configuration
     */
    public static HikariConfig getConfigWithProperties(@NotNull DataSourceType type,
                                                       @Nullable Properties customProperties) {
        HikariConfig configuration;

        if (customProperties == null && SignShopConfig.getUseCustomDatabaseConfig()) {
            // We only use the users custom database config if we aren't using our own
            configuration = new HikariConfig(new File(SignShop.getInstance().getDataFolder(), "database.properties").getPath());
        } else {
            configuration = new HikariConfig(customProperties);
        }

        // Basic pool configuration
        configuration.setMinimumIdle(SignShopConfig.getDatabaseMinimumIdle());
        configuration.setMaximumPoolSize(SignShopConfig.getDatabaseMaximumPoolSize());
        configuration.setDataSourceClassName(type.getDataSourceClass());

        // If we configured with custom properties we're done
        if (customProperties != null) return configuration;

        // Auth: don't add blank paramaters or hibernate might get confused
        String user = SignShopConfig.getDatabaseAuthenticationUsername();
        String password = SignShopConfig.getDatabaseAuthenticationPassword();

        if (user != null && !user.trim().isEmpty())
            configuration.setUsername(user);

        if (password != null && !password.trim().isEmpty())
            configuration.setPassword(password);

        // Push the remaining args in without overwriting or duplicating
        // We need a URL
        configuration.setJdbcUrl(SignShopConfig.getDatabaseConnectionUrl());

        return configuration;
    }

    /**
     * Apply some Hikari settings that optimize performance on pure MySQL implementations
     *
     * @param builder The builder to apply the settings to
     */
    public static void applyMySqlOptimizations(@NotNull HikariConfig builder) {
        builder.applySettings(MYSQL_OPTIMIZATIONS);
    }

    /**
     * Validate that the current database connection is allowed to succeed the previous connection
     * <br><h3>Invalid states: </h3><ul>
     * <li>If the previous session had a database, and the current does not</li>
     * <li>OR Both sessions are using a database, and the connections do not match</li>
     * </ul>
     *
     * @param previous The previous database properties
     * @param current  The current database properties
     * @return If the state between previous and current is valid
     */
    public static boolean isConnectionChangeOkay(HikariConfig previous, HikariConfig current) {
        // Connection is OK if it's new
        if (previous == null) return true;

        // Connection is not OK if previous != null and current == null
        if (current == null) return false;

        // Same thing for the urls
        String previousUrl = previous.getJdbcUrl();
        String currentUrl = current.getJdbcUrl();

        // Connection is OK if it's new
        if (previousUrl == null) return true;

        // Connection is not OK if previous != null and current == null
        if (currentUrl == null) return false;

        // Connection is ok if the URLs match
        return previousUrl.equalsIgnoreCase(currentUrl);
    }
}
