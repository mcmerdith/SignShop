package org.wargamer2010.signshop.configuration.storage;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.storage.database.InternalDatabase;
import org.wargamer2010.signshop.configuration.storage.database.models.SignShopSchema;
import org.wargamer2010.signshop.configuration.storage.database.util.DatabaseUtil;
import org.wargamer2010.signshop.configuration.storage.database.util.SSQueryBuilder;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.util.*;

public class DatabaseDataSource extends Storage implements InternalDatabase {
    private static final SignShopLogger logger = SignShopLogger.getDatabaseLogger();

    public static final int DATABASE_VERSION = 1;

    public final SessionFactory sessionFactory;
    public final SessionFactory sqliteFactory;

    private final SignShopConfig.DataSourceType type;
    private final Properties customProperties;

    /**
     * If the database can only perform built-in storage
     *
     * @return true: Internal Storage Only, false: Sellers are being stored
     */
    private boolean internalFunctionsOnly() {
        return sessionFactory == null;
    }

    private final Map<Location, Seller> sellers = new HashMap<>();

    /**
     * Get a connection to a DataSource without loading SignShop internals. Should only be used for a migration. Database is connected and ready as soon as the class is created
     *
     * @throws IllegalStateException If the database is unavailable
     */
    public DatabaseDataSource(SignShopConfig.DataSourceType dataSource, Properties customProperties) throws IllegalStateException {
        this.type = dataSource;
        this.customProperties = customProperties;

        sqliteFactory = null;
        sessionFactory = getSessionFactory();
        schema = null;
    }

    /**
     * Construct a new Database Data Source. Database is connected and ready as soon as the class is created
     *
     * @throws IllegalStateException If the database is unavailable
     */
    public DatabaseDataSource(SignShopConfig.DataSourceType pluginDataSource) throws IllegalStateException {
        this.type = pluginDataSource;
        this.customProperties = null;

        // Build our sqLite database. Default SQLite config is available on the classpath as "hibernate.cfg.xml"
        // This will become the final database if using SQLite, otherwise it will be disposed of after reading the schema
        sqliteFactory = getSqliteFactory();

        logger.info("Loading and validating schema...");

        // Attempt to load the schema
        schema = readSchema();

        // Save the schema if the validator updates it
        if (schema.validate()) saveSchema();

//        if (schema.needsVersionConversion()) {
//            SignShopLogger conversionLogger = new SignShopLogger("Database Converter");
//
//            conversionLogger.info("Converting data from v" + schema.getDatabaseVersion() + " to v" + DATABASE_VERSION);
//            conversionLogger.info("Complete!");
//        }

        sessionFactory = getSessionFactory();
    }

    private SessionFactory getSqliteFactory() {
        // Don't make more than 1
        if (sqliteFactory != null) return sqliteFactory;

        final StandardServiceRegistry sqLiteRegistry = DatabaseUtil.configureInternal(new StandardServiceRegistryBuilder()).build();

        try {
            // Only prepare the schema if we're not using SQLite for the main database
            return DatabaseUtil.getSessionFactory(type, sqLiteRegistry, true);
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(sqLiteRegistry);
            throw logger.exception(e, "Failed to connect to SQLite database!", true);
        }
    }

    /**
     * Get a database SessionFactory
     *
     * @return The SessionFactory
     * @throws IllegalStateException If the database fails to connect
     */
    private SessionFactory getSessionFactory() {
        // Don't make more than 1
        if (sessionFactory != null) return sessionFactory;

        // Load the database if necessary
        if (type == SignShopConfig.DataSourceType.YML) {
            // We are not using a database for storage
            return null;
        } else {
            // We are using a database, prep it
            logger.info("Loading " + type.name() + " database...");

            if (type == SignShopConfig.DataSourceType.SQLITE) {
                // If we are using SQLite we can reuse the SQLite SessionFactory
                return getSqliteFactory();
            } else {
                StandardServiceRegistryBuilder mainDatabaseBuilder = DatabaseUtil.configureWithProperties(new StandardServiceRegistryBuilder(), type, customProperties);

                // Optimize pure MySql
                if (type == SignShopConfig.DataSourceType.MYSQL)
                    DatabaseUtil.applyMySqlOptimizations(mainDatabaseBuilder);

                // Build the SessionFactory
                final StandardServiceRegistry mainRegistry = mainDatabaseBuilder.build();

                try {
                    return DatabaseUtil.getSessionFactory(type, mainRegistry);
                } catch (Exception e) {
                    StandardServiceRegistryBuilder.destroy(mainRegistry);
                    throw logger.exception(e, "Failed to connect to " + type.name() + "database!", true);
                }
            }
        }
    }

    @Override
    public SignShopConfig.DataSourceType getType() {
        return type;
    }

    /**
     * Get the properties associated with the database
     * @return The database custom properties, or null if they are not configured
     */
    public Properties getCustomProperties() {
        return customProperties;
    }

    /*
    Seller Storage
     */

    @Override
    public boolean loadSellers() {
        try (Session session = sessionFactory.getCurrentSession()) {
            // Begin transaction on the database
            session.beginTransaction();

            // Get the sellers
            List<Seller> tempSellers = session.createQuery("from Seller ", Seller.class).getResultList();
        }

        return false;
    }

    @Override
    public boolean saveSellers() {
        return false;
    }

    @Override
    public void dispose() {
        // dispose of session
        if (sessionFactory != null && !sessionFactory.isClosed()) sessionFactory.close();
    }

    @Override
    public int shopCount() {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return sellers.size();
    }

    @Override
    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        Seller newSeller = new Seller(playerId, sWorld, containables, activatables, isItems, bSign.getLocation(), misc, save);
        sellers.put(bSign.getLocation(), newSeller);

        if (save) {
            try (Session session = sessionFactory.getCurrentSession()) {
                session.beginTransaction();
                session.persist(newSeller);
                session.getTransaction().commit();
            } catch (HibernateException e) {
                logger.exception(e, "Failed to save seller to database!");
            }
        }
    }

    @Override
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        // TODO prepare query and queue it on the worker
    }

    @Override
    public Seller getSeller(Location lKey) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return sellers.getOrDefault(lKey, null);
    }

    @Override
    public Collection<Seller> getSellers() {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return sellers.values();
    }

    @Override
    public void removeSeller(Location lKey) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        // Remove the seller from our local map
        sellers.remove(lKey);

        // TODO prepare query and queue it on the worker
    }

    @Override
    public int countLocations(SignShopPlayer player) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return 0;
    }

    @Override
    public List<Block> getSignsFromHolder(Block bHolder) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return null;
    }

    @Override
    public List<Seller> getShopsByBlock(Block bBlock) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return null;
    }

    @Override
    public List<Block> getShopsWithMiscSetting(String key, String value) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);
        return null;
    }

    /*
    Internal Database
     */

    private final SignShopSchema schema;

    /**
     * Get the schema for this server. If one does not exist in the database create one
     *
     * @return The schema
     */
    private SignShopSchema readSchema() {
        return readSchema(true);
    }

    /**
     * Find a schema for this server, or create one
     *
     * <br>You probably want {@link DatabaseDataSource#readSchema()}
     *
     * @param strictMatch Starting strictness
     * @return The schema
     */
    private SignShopSchema readSchema(boolean strictMatch) {
        SignShopSchema tempSchema;

        // This servers identifier
        String server = SignShopConfig.getDatabaseServerName();

        try (Session session = sqliteFactory.getCurrentSession()) {
            // Begin transaction on the database
            session.getTransaction().begin();

            // Set up the query
            CriteriaBuilder builder = session.getCriteriaBuilder();
            SSQueryBuilder<SignShopSchema> schemaQuery = new SSQueryBuilder<>(builder, SignShopSchema.class);

            if (strictMatch) {
                // Strict match means the 'server' field must match
                schemaQuery.criteria.where(builder.equal(schemaQuery.root.get("server"), server));
            } else {
                // Otherwise it will find any schema registered by plugin 'signshop'
                schemaQuery.criteria.where(builder.equal(schemaQuery.root.get("plugin"), "signshop"));
            }

            // Build the query
            Query<SignShopSchema> query = schemaQuery.build(session);

            try {
                // Find a unique schema
                tempSchema = query.uniqueResult();

                if (tempSchema == null) {
                    if (strictMatch) {
                        // If no match, try to find schemas by plugin instead of server name
                        session.getTransaction().rollback();
                        return readSchema(false);
                    } else {
                        // No match found by plugin or server, we'll need to make a new one
                        logger.info("No server schema found. This is probably your first startup. Creating schema...");

                        // We created it so it's a strict match
                        strictMatch = true;
                        tempSchema = new SignShopSchema(
                                server,
                                DATABASE_VERSION,
                                SignShopConfig.DataSourceType.YML,
                                null);
                        session.persist(tempSchema);
                    }
                }
            } catch (NonUniqueResultException ignored) {
                int matches = query.getResultList().size();

                // More than 1, assume the first. Shouldn't be possible on strict match because "server" is a unique key but hey, fail gracefully I guess
                logger.error((strictMatch)
                        ? String.format("%d schemas registered for server '%s'! This should not happen", matches, server)
                        : String.format("No schema for server '%s', but found %d other schemas. 'signshop.db' likely does not belong to this server, or is corrupt.", server, matches));

                tempSchema = query.getResultList().get(0);
            }

            if (!strictMatch) {
                logger.error(String.format("Could not find schema for server '%s', but found schema server '%s'! Reassigning...", server, tempSchema.getServer()));
                logger.error("SignShop may be running on the wrong server. If you changed 'Database.ServerName' in config.yml you can ignore this message");

                // Update the schema
                tempSchema.setServer(server);
            }

            session.getTransaction().commit();

            return tempSchema;
        } catch (Exception e) {
            throw logger.exception(e, "Failed to connect to schema database!", true);
        }
    }

    /**
     * Get the external database (if present) or fallback to a guaranteed SQLite connection
     * Internal functions other than the schema will use this connection
     *
     * @return A valid SessionFactory
     */
    private SessionFactory databaseOrSqlite() {
        if (sessionFactory != null) return sessionFactory;
        return sqliteFactory;
    }

    @Override
    public SignShopSchema getSchema() {
        return schema;
    }

    @Override
    public void saveSchema() {
        try (Session session = sqliteFactory.getCurrentSession()) {
            // Try to open a session
            session.getTransaction().begin();

            // Merge it if not already merged
            if (!session.contains(schema)) session.merge(schema);

            // Close the session
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.exception(e, "Failed to connect to schema database!");
        }
    }
}
