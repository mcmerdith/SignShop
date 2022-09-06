package org.wargamer2010.signshop.configuration.storage.database.models;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.storage.DatabaseDataSource;
import org.wargamer2010.signshop.configuration.storage.database.datatype.PropertiesConverter;
import org.wargamer2010.signshop.configuration.storage.database.util.DatabaseUtil;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

@Entity
@Table(name = "signshop_master")
public class SignShopSchema {
    /*
    Fields
     */

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    /**
     * Always 'signshop';
     */
    @Basic(optional = false)
    @Column(unique = true)
    private String plugin = "signshop";

    /**
     * The unique name for the server
     */
    @Basic(optional = false)
    @Column(unique = true)
    private String server;

    /**
     * The version of the database that was being used
     */
    @Basic(optional = false)
    private Integer databaseVersion;

    @Enumerated(value = EnumType.STRING)
    @Basic(optional = false)
    private DataSourceType dataSource;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = PropertiesConverter.class)
    private Properties connectionProperties;

    /*
    Setters
     */

    public void setServer(String server) {
        this.server = server;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public void setDataSource(DataSourceType dataSource) {
        this.dataSource = dataSource;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /*
    Getters
     */

    /**
     * @return The identifier for this server
     */
    public String getServer() {
        return server;
    }

    /**
     * @return The version of the database being used
     */
    public int getDatabaseVersion() {
        return databaseVersion;
    }

    /**
     * @return The Storage implementation used last time SignShop was running
     */
    public DataSourceType getDataSource() {
        return dataSource;
    }

    /**
     * @return The last url that was used to connect to the database
     */
    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    /*
    Validation
     */

    @Transient
    private boolean databaseVersionOK = true;
    @Transient
    private boolean dataSourceOK = true;
    @Transient
    private boolean connectionOK = true;

    public boolean needsVersionConversion() {
        return !databaseVersionOK;
    }

    public boolean needsStorageMigration() {
        return !dataSourceOK;
    }

    public boolean needsDatabaseMigration() {
        return !connectionOK;
    }

    /**
     * Validate that the current schema matches this one
     *
     * @return If the schema was modified during validation and needs to be saved
     */
    public boolean validate() {
        boolean changed = false;

        SignShopLogger logger = SignShopLogger.getLogger("Validation");

        // Current session info
        int CURRENT_VERSION = DatabaseDataSource.DATABASE_VERSION;
        DataSourceType CURRENT_SOURCE = SignShopConfig.getDataSource();
        Properties CURRENT_CONNECTION = DatabaseUtil.getDatabaseProperties(true);


        // Check if the database revision is different
        if (databaseVersion < 1) {
            // Probably a glitch? Correct it
            logger.error("Invalid Database Version: v" + databaseVersion + ". Assuming v" + CURRENT_VERSION);
            databaseVersion = CURRENT_VERSION;
            changed = true;
        } else if (databaseVersion > CURRENT_VERSION) {
            throw logger.exception(null, String.format("Database version is not supported! Is SignShop up to date? (Supported: v%d < v%d)", CURRENT_VERSION, databaseVersion), true);
        } else if (databaseVersion < CURRENT_VERSION) {
            databaseVersionOK = false;
            logger.info(String.format("Database is out of date (Supported: v%d > v%d)! SignShop will attempt to convert existing data", CURRENT_VERSION, databaseVersion));
        }


        // Validate that we are connected to the same database
        if (!DatabaseUtil.isConnectionChangeOkay(connectionProperties, CURRENT_CONNECTION)) {
            connectionOK = false;
            logger.error("Database connection information does not match last session! The database likely needs a migration!");
            logger.info("   Saving migration data to database_migration.properties... Please migrate the database yourself, or try the '/signshop MigrateDatabase' command");

            try {
                File migrationData = new File(SignShop.getInstance().getDataFolder(), String.format("database_migration%d.properties", System.currentTimeMillis()));
                FileOutputStream fs = new FileOutputStream(migrationData);
                connectionProperties.store(fs, "Migration Data");
                fs.close();
            } catch (Exception e) {
                logger.exception(e, "Failed to write migration file!");
            }
        }


        // Validate that we are using the same Storage and DB connection as last time
        if (dataSource != CURRENT_SOURCE) {
            dataSourceOK = false;

            // If the connection changed, we can't automatically migrate the data
            logger.error(
                    String.format("Last used data source was %s, but we are currently using %s%s",
                            dataSource.name(),
                            CURRENT_SOURCE.name(),
                            connectionOK ? ". SignShop will attempt to migrate the data" : ""
                    ));
        }


        if (databaseVersionOK && connectionOK && dataSourceOK) logger.info("All validations passed!");

        return changed;
    }

    /**
     * JPA Constructor
     */
    protected SignShopSchema() {
    }

    /**
     * Basic information about SignShop and its Storage state
     *
     * @param server               The identifier for this server
     * @param databaseVersion      The version of the database being used
     * @param dataSource           The Storage implementation used last time SignShop was running
     * @param connectionProperties The last url that was used to connect to the database
     */
    public SignShopSchema(String server, int databaseVersion, DataSourceType dataSource, Properties connectionProperties) {
        this.server = server;
        this.databaseVersion = databaseVersion;
        this.dataSource = dataSource;
        this.connectionProperties = connectionProperties;
    }
}
