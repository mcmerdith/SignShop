package org.wargamer2010.signshop.configuration.storage.database.util;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.Action;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.SignShopLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseUtil {
    private static final Properties hibernateConfigurationBase = new Properties();
    private static final Properties sqliteProperties = new Properties();
    private static final Map<String, Object> MYSQL_OPTIMIZATIONS = new HashMap<>();

    public static final String HIBERNATE_URL_KEY = "hibernate.hikari.dataSource.url";

    static {
        // Build the static parameters
        hibernateConfigurationBase.put("hibernate.current_session_context_class", "thread");
        hibernateConfigurationBase.put("hibernate.hbm2ddl.auto", Action.UPDATE.getExternalHbm2ddlName());

        sqliteProperties.putAll(hibernateConfigurationBase);
        sqliteProperties.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
        try {
            sqliteProperties.put("hibernate.connection.url", "jdbc:sqlite:" +
                    new File(SignShop.getInstance().getDataFolder(), "signshop.db").getPath());
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().error("Unable to reserve file 'signshop.db' in the plugin folder!");
            sqliteProperties.put("hibernate.connection.url", "jdbc:sqlite:signshop.db");
        }

        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.prepStmtCacheSize", "250");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.prepStmtCacheSqlLimit", "2048");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.cachePrepStmts", "true");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.useServerPrepStmts", "true");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.useLocalSessionState", "true");
//        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.rewriteBatchedStatements", true); Hibernate should already be doing this
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.cacheResultSetMetadata", "true");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.cacheServerConfiguration", "true");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.elideSetAutoCommits", "true");
        MYSQL_OPTIMIZATIONS.put("hibernate.hikari.dataSource.maintainTimeStats", String.valueOf(SignShopConfig.debugging())); // Only enable Query Time stats if we are debugging
    }

    /**
     * Configure the database builder for SQLite
     *
     * @param builder Builder to configure
     * @return A SQLite database builder
     */
    public static StandardServiceRegistryBuilder configureInternal(@NotNull StandardServiceRegistryBuilder builder) {
        return builder.applySettings(sqliteProperties);
    }

    /**
     * Configure the database builder
     *
     * @param builder Builder to configure
     * @return A database builder
     */
    public static StandardServiceRegistryBuilder configure(@NotNull StandardServiceRegistryBuilder builder) {
        if (SignShopConfig.getDataSource().isExternal()) {
            return configure(builder, null, null);
        } else {
            return configureInternal(builder);
        }
    }

    /**
     * Configure a database builder
     *
     * @param builder          Builder to configure
     * @param customSource     The source to apply the custom properties to
     * @param customProperties Custom properties to apply, does nothing if internal is set
     * @return A database builder with the specified properties
     */
    public static StandardServiceRegistryBuilder configure(@NotNull StandardServiceRegistryBuilder builder,
                                                           @Nullable DataSourceType customSource,
                                                           @Nullable Properties customProperties) {
        // Configure from hibernate.cfg.xml or Properties
        return builder.applySettings(
                customSource != null && customProperties != null // Must have both to be able to apply properties
                        ? getDatabaseWithCustomProperties(customSource, false, customProperties)
                        : getDatabaseProperties(false)
        );
    }

    /**
     * Get the configuration for the set database
     *
     * @param onlyConnectionProperties Only return the connection properties
     * @return The configuration
     */
    public static Properties getDatabaseProperties(boolean onlyConnectionProperties) {
        return getDatabaseWithCustomProperties(SignShopConfig.getDataSource(), onlyConnectionProperties, null);
    }

    /**
     * Get a configuration for a database
     *
     * @param type                     The type of database being configured
     * @param onlyConnectionProperties Only return the connection properties, does nothing if custom properties are set
     * @param customProperties         Custom properties for the databse
     * @return The configuration
     */
    public static Properties getDatabaseWithCustomProperties(@NotNull DataSourceType type,
                                                             boolean onlyConnectionProperties,
                                                             @Nullable Properties customProperties) {
        Properties configuration = new Properties();

        configuration.putAll(hibernateConfigurationBase);

        // Using Hikari
        configuration.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        // Pool Config
        configuration.put("hibernate.hikari.minimumIdle", String.valueOf(SignShopConfig.getDatabaseMinimumIdle()));
        configuration.put("hibernate.hikari.maximumPoolSize", String.valueOf(SignShopConfig.getDatabaseMaximumPoolSize()));
        configuration.put("hibernate.hikari.dataSourceClassName", type.getDataSourceClass());

        // Load the custom properties or default properties
        if (customProperties == null) {
            // Auth: don't add blank paramaters or hibernate might get confused
            String userKey = "hibernate.hikari.dataSource.user";
            String passKey = "hibernate.hikari.dataSource.password";

            String user = SignShopConfig.getDatabaseAuthenticationUsername();
            String password = SignShopConfig.getDatabaseAuthenticationPassword();

            if (user != null && !user.trim().isEmpty())
                configuration.put(userKey, user);

            if (password != null && !password.trim().isEmpty())
                configuration.put(passKey, password);

            // Read the custom configuration (if enabled)
            if (SignShopConfig.getUseCustomDatabaseConfig()) {
                // Attempt to load the config
                File config = new File(SignShop.getInstance().getDataFolder(), "database.properties");

                try (InputStream reader = new FileInputStream(config)) {
                    configuration.load(reader);
                } catch (Exception e) {
                    SignShopLogger.getDatabaseLogger().exception(e, "Failed to read custom config file! SignShop will use database config defaults");
                }
            }

            // Push the remaining args in without overwriting or duplicating
            // We need a URL
            if (!configuration.containsKey(HIBERNATE_URL_KEY))
                configuration.put(HIBERNATE_URL_KEY, SignShopConfig.getDatabaseConnectionUrl());

            if (onlyConnectionProperties) {
                // Build the connectionProperties
                Properties connectionProperties = new Properties();
                if (configuration.containsKey(userKey)) connectionProperties.put(userKey, configuration.get(userKey));
                if (configuration.containsKey(passKey)) connectionProperties.put(passKey, configuration.get(passKey));
                connectionProperties.put(HIBERNATE_URL_KEY, configuration.get(HIBERNATE_URL_KEY));

                return connectionProperties;
            }
        } else {
            configuration.putAll(customProperties);
        }

        return configuration;
    }

    /**
     * Apply some Hikari settings that optimize performance on pure MySQL implementations
     *
     * @param builder The builder to apply the settings to
     */
    public static void applyMySqlOptimizations(@NotNull StandardServiceRegistryBuilder builder) {
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
    public static boolean isConnectionChangeOkay(Properties previous, Properties current) {
        // Connection is OK if both are null
        if (previous == null) return true;

        // Connection is not OK if previous != null and current == null
        if (current == null) return false;

        if (!current.containsKey(HIBERNATE_URL_KEY) || !previous.containsKey(HIBERNATE_URL_KEY)) return false;

        Object prev = previous.get(HIBERNATE_URL_KEY);
        Object curr = current.get(HIBERNATE_URL_KEY);

        // Connection is OK if both are null
        if (prev == null) return true;

        // Connection is not OK if prev != null and curr == null
        if (curr == null) return false;

        // Connection is ok if the URLs match
        return prev.equals(current);
    }
}
