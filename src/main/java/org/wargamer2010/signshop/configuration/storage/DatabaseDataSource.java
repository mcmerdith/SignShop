package org.wargamer2010.signshop.configuration.storage;

import com.zaxxer.hikari.HikariConfig;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.storage.database.InternalDatabase;
import org.wargamer2010.signshop.configuration.storage.database.SSSessionFactory;
import org.wargamer2010.signshop.configuration.storage.database.models.SignShopSchema;
import org.wargamer2010.signshop.configuration.storage.database.util.LazyLocation;
import org.wargamer2010.signshop.configuration.storage.database.util.SSQueryBuilder;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.signshopUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DatabaseDataSource extends Storage implements InternalDatabase {
    private static final SignShopLogger logger = SignShopLogger.getDatabaseLogger();

    public static final int DATABASE_VERSION = 1;

    public final SessionFactory sessionFactory;
    public final SessionFactory sqliteFactory;

    private final DataSourceType type;
    private final HikariConfig customConfig;

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
    public DatabaseDataSource(DataSourceType dataSource, HikariConfig config) throws IllegalStateException {
        this.type = dataSource;
        this.customConfig = config;

        logger.info("Initializing new Transfer DatabaseDataSource for " + type.name());

        sqliteFactory = null;
        sessionFactory = SSSessionFactory.getDatabaseFactory(type, customConfig);
        schema = null;
    }

    /**
     * Construct a new Database Data Source. Database is connected and ready as soon as the class is created
     *
     * @throws IllegalStateException If the database is unavailable
     */
    public DatabaseDataSource(DataSourceType pluginDataSource) throws IllegalStateException {
        this.type = pluginDataSource;
        this.customConfig = null;

        // Build our sqLite database. Default SQLite config is available on the classpath as "hibernate.cfg.xml"
        // This will become the final database if using SQLite, otherwise it will be disposed of after reading the schema
        sqliteFactory = SSSessionFactory.getSqliteFactory(type);

        logger.info("Loading and validating schema...");

        // Attempt to load the schema
        schema = getSchema();

        // Save the schema if the validator updates it
        if (schema.validate()) saveSchema();

        sessionFactory = SSSessionFactory.getDatabaseFactory(type, null);
    }


    @Override
    public DataSourceType getType() {
        return type;
    }

    /**
     * Get the properties associated with the database
     *
     * @return The database custom properties, or null if they are not configured
     */
    public Properties getCustomConfig() {
        return customConfig;
    }

    /*
    Seller Storage
     */

    @Override
    protected Map<Location, Seller> getSellerMap() {
        return sellers;
    }

    @Override
    public void dispose() {
        // dispose of session
        if (sqliteFactory != null && !sqliteFactory.isClosed()) sqliteFactory.close();
        if (sessionFactory != null && !sessionFactory.isClosed()) sessionFactory.close();
    }

    @Override
    public boolean loadSellers() {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);

        try (Session session = sessionFactory.getCurrentSession()) {
            // Begin transaction on the database
            session.beginTransaction();

            // Get the sellers
            List<Seller> tempSellers = session.createQuery("from Seller", Seller.class).getResultList();

            for (Seller seller : tempSellers) sellers.put(seller.getSignLocation(), seller);

            logger.info(String.format("Loaded %d shops!", shopCount()));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.exception(e, "Failed to load sellers from database");
            return false;
        }
    }

    @Override
    public void saveSellers() {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);


        for (Seller seller : sellers.values()) {
            try (Session session = sessionFactory.getCurrentSession()) {
                try {
                    session.getTransaction().begin();
                    if (!session.contains(seller)) session.merge(seller);
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    logger.exception(e, String.format("Failed to save seller at %s to database",
                            signshopUtil.convertLocationToString(seller.getSignLocation())));
                }
                session.getTransaction().commit();
            } catch (Exception e) {
                logger.exception(e, "Failed to save sellers to database");
            }
        }
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
                logger.exception(e, String.format("Failed to save seller at %s!", signshopUtil.convertLocationToString(bSign.getLocation())));
            }
        }
    }

    @Override
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, @Nullable ItemStack[] isItems) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);

        Location signLocation = bSign.getLocation();

        if (!sellers.containsKey(signLocation)) {
            // If it isn't in the map try and retrieve it
            try (Session session = sessionFactory.getCurrentSession()) {
                session.getTransaction().begin();

                CriteriaBuilder builder = session.getCriteriaBuilder();
                SSQueryBuilder<Seller> query = new SSQueryBuilder<>(builder, Seller.class);
                query.criteria.where(builder.equal(query.root.get("sign"), new LazyLocation(signLocation)));

                Seller tempSeller = session.createQuery(query.criteria).uniqueResult();

                if (tempSeller == null) {
                    // Failed to retrieve from database
                    session.getTransaction().rollback();
                    throw new RuntimeException("Seller does not exist");
                }

                sellers.put(signLocation, tempSeller);

                session.getTransaction().commit();
            } catch (Exception e) {
                logger.exception(e, String.format("Failed to update seller at %s!", signshopUtil.convertLocationToString(bSign.getLocation())));
                return;
            }
        }

        // Update

        Seller toUpdate = sellers.get(signLocation);

        toUpdate.setContainables(containables);
        toUpdate.setActivatables(activatables);
        if (isItems != null) toUpdate.setItems(isItems);

        // Save to DB

        try (Session session = sessionFactory.getCurrentSession()) {
            session.getTransaction().begin();

            if (!session.contains(toUpdate)) session.merge(toUpdate);

            session.getTransaction().commit();
        } catch (Exception e) {
            logger.exception(e, String.format("Failed to update seller at %s!", signshopUtil.convertLocationToString(bSign.getLocation())));
        }
    }

    @Override
    public void removeSeller(Location lKey) {
        if (internalFunctionsOnly())
            throw logger.exception(null, "Seller data cannot be accessed in internal mode", true);

        Seller toRemove = sellers.get(lKey);

        try (Session session = sessionFactory.getCurrentSession()) {
            session.getTransaction().begin();

            if (!session.contains(toRemove)) session.merge(toRemove);

            session.remove(toRemove);

            session.getTransaction().commit();
        } catch (Exception e) {
            logger.exception(e, String.format("Failed to remove seller at %s!", signshopUtil.convertLocationToString(toRemove.getSignLocation())));
        }

        sellers.remove(lKey);
    }

    /*
    Internal Database
     */

    private final SignShopSchema schema;

    @Override
    public SignShopSchema getSchema() {
        return schema == null ? readSchema() : schema;
    }

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
                                DataSourceType.YML,
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
}
