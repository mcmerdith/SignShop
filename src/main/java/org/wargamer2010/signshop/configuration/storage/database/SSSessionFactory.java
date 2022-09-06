package org.wargamer2010.signshop.configuration.storage.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.storage.database.models.SellerExport;
import org.wargamer2010.signshop.configuration.storage.database.models.SignShopSchema;
import org.wargamer2010.signshop.configuration.storage.database.util.DatabaseUtil;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.util.Properties;

public class SSSessionFactory {
    /*
    Session Factory
     */

    private static final SignShopLogger logger = SignShopLogger.getLogger("Session Factory");

    public static SessionFactory getSqliteFactory(DataSourceType type) {
        final StandardServiceRegistry sqliteRegistry = DatabaseUtil.configureInternal(new StandardServiceRegistryBuilder()).build();

        try {
            // Only prepare the schema if we're not using SQLite for the main database
            return getSessionFactory(type, sqliteRegistry, true);
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(sqliteRegistry);
            throw logger.exception(e, "Failed to connect to SQLite database!", true);
        }
    }

    /**
     * Get a database SessionFactory
     *
     * @return The SessionFactory
     * @throws IllegalStateException If the database fails to connect
     */
    public static SessionFactory getDatabaseFactory(DataSourceType type, Properties customProperties) {
        // Load the database if necessary
        if (type == DataSourceType.YML) {
            // We are not using a database for storage
            return null;
        } else {
            // We are using a database, prep it
            logger.info("Loading " + type.name() + " database...");

            if (type == DataSourceType.SQLITE) {
                // If we are using SQLite we can reuse the SQLite SessionFactory
                return getSqliteFactory(type);
            } else {
                StandardServiceRegistryBuilder mainDatabaseBuilder = DatabaseUtil.configure(new StandardServiceRegistryBuilder(), type, customProperties);

                // Optimize pure MySql
                if (type == DataSourceType.MYSQL)
                    DatabaseUtil.applyMySqlOptimizations(mainDatabaseBuilder);

                // Build the SessionFactory
                final StandardServiceRegistry mainRegistry = mainDatabaseBuilder.build();

                try {
                    return getSessionFactory(type, mainRegistry);
                } catch (Exception e) {
                    StandardServiceRegistryBuilder.destroy(mainRegistry);
                    throw logger.exception(e, "Failed to connect to " + type.name() + " database!", true);
                }
            }
        }
    }


    /**
     * Get a standard SessionFactory
     *
     * @param type     The type of database for the SessionFactory
     * @param registry The ServiceRegistry to build the SessionFactory with
     * @return The SessionFactory
     */
    public static SessionFactory getSessionFactory(@NotNull DataSourceType type, @NotNull ServiceRegistry registry) {
        return getSessionFactory(type, registry, false);
    }

    /**
     * Get a SessionFactory with annotated Entity classes
     * <br>SQlite databases contain only the schema tables UNLESS the Storage type is SQlite
     *
     * @param type     The type of database for the SessionFactory
     * @param registry The ServiceRegistry to build the SessionFactory with
     * @param sqlite   If the SessionFactory is for sqltie
     * @return The SessionFactory
     */
    public static SessionFactory getSessionFactory(@NotNull DataSourceType type, @NotNull ServiceRegistry registry, boolean sqlite) {
        MetadataSources metadata = new MetadataSources(registry);

        if (sqlite) {
            // Only the schema is stored in the sqlite db
            metadata.addAnnotatedClass(SignShopSchema.class);
        }

        if (!sqlite || type == DataSourceType.SQLITE) {
            // Everything else is stored in the main db (could be sqlite)
            metadata.addAnnotatedClasses(Seller.class, SellerExport.class);
        }

        // Build the session factory. Naming strategy dictates that all tables (except `signshop_master`) will be prefixed with signshop_servername_
        return metadata.getMetadataBuilder().applyPhysicalNamingStrategy(new SignShopNamingStrategy())
                .build().buildSessionFactory();
    }
}
