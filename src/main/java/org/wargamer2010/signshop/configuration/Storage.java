package org.wargamer2010.signshop.configuration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.orm.typing.conversion.HikariConfigConverter;
import org.wargamer2010.signshop.configuration.storage.DatabaseDataSource;
import org.wargamer2010.signshop.configuration.storage.YMLDataSource;
import org.wargamer2010.signshop.configuration.storage.database.InternalDatabase;
import org.wargamer2010.signshop.configuration.storage.database.models.SignShopSchema;
import org.wargamer2010.signshop.configuration.storage.database.util.DatabaseUtil;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public abstract class Storage {
    /**
     * Get the Storage implementation this DataSource represents
     *
     * @return The implemented datasource
     */
    abstract public DataSourceType getType();

    /**
     * The Seller map this storage implementation is using
     *
     * @return The Location -> Seller map for this instance
     */
    abstract protected Map<Location, Seller> getSellerMap();

    /**
     * Clean up any data before closing
     */
    abstract public void dispose();

    /**
     * Load SignShops Data
     *
     * @return If the Storage needs to be saved
     */
    abstract public boolean loadSellers();

    /**
     * Renamed to {@link Storage#loadSellers()}
     *
     * @deprecated Will be removed at a later date
     */
    @Deprecated
    final public boolean Load() {
        return loadSellers();
    }

    /**
     * Save SignShops Data
     */
    abstract public void saveSellers();

    /**
     * Renamed to {@link Storage#saveSellers()}
     *
     * @deprecated Will be removed at a later date
     */
    @Deprecated
    final public void Save() {
        saveSellers();
    }

    /**
     * Save a new shop
     *
     * @param playerId     Player owner
     * @param sWorld       The world the shop is in
     * @param bSign        The sign
     * @param containables A list of containers the shop can draw from/deposit to
     * @param activatables A list of blocks the shop can "activate" (for [Device] signs etc)
     * @param isItems      The items the shop was configured with (items to take for [Sell], items to give for [Buy], etc)
     * @param misc         Any miscellaneous properties
     */
    final public void addSeller(@Nonnull PlayerIdentifier playerId, @Nonnull String sWorld, @Nonnull Block bSign, @Nonnull List<Block> containables, @Nonnull List<Block> activatables, @Nonnull ItemStack[] isItems, @Nonnull Map<String, String> misc) {
        addSeller(playerId, sWorld, bSign, containables, activatables, isItems, misc, true);
    }

    /**
     * Add a new shop
     *
     * @param playerId     Player owner
     * @param sWorld       The world the shop is in
     * @param bSign        The sign
     * @param containables A list of containers the shop can draw from/deposit to
     * @param activatables A list of blocks the shop can "activate" (for [Device] signs etc)
     * @param isItems      The items the shop was configured with (items to take for [Sell], items to give for [Buy], etc)
     * @param misc         Any miscellaneous properties
     * @param save         If we should save
     */
    abstract public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save);

    /**
     * Update a shop
     *
     * @param bSign        The new sign
     * @param containables A list of containers the shop can draw from/deposit to
     * @param activatables A list of blocks the shop can "activate" (for [Device] signs etc)
     */
    final public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables) {
        updateSeller(bSign, containables, activatables, null);
    }

    /**
     * Update a shop
     *
     * @param bSign        The new sign
     * @param containables A list of containers the shop can draw from/deposit to
     * @param activatables A list of blocks the shop can "activate" (for [Device] signs etc)
     * @param isItems      The items the shop was configured with (items to take for [Sell], items to give for [Buy], etc)
     */
    abstract public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, @Nullable ItemStack[] isItems);

    /**
     * Delete a shop
     *
     * @param lKey The location of the shop
     */
    abstract public void removeSeller(Location lKey);

    /*
    Data and Lookup functions are the same no matter how the data is stored
     */

    /**
     * How many shops are loaded
     *
     * @return The number of shops loaded
     */
    final public int shopCount() {
        return getSellerMap().size();
    }

    /**
     * Get a seller
     *
     * @param lKey The location of the sign
     * @return The seller, or null if not found
     */
    final public Seller getSeller(Location lKey) {
        if (getSellerMap().containsKey(lKey))
            return getSellerMap().get(lKey);
        return null;
    }

    /**
     * Get all currently loaded sellers
     *
     * @return A collection of sellers
     */
    final public Collection<Seller> getSellers() {
        return Collections.unmodifiableCollection(getSellerMap().values());
    }

    /**
     * Get the sign associated with a shop
     *
     * @param pSeller The shop
     * @return The sign
     * @deprecated Shops store their own sign location, this is only a legacy helper method
     */
    @Deprecated
    final public Block getSignFromSeller(Seller pSeller) {
        return pSeller.getSign();
    }

    /**
     * Get how many shops are owned by a player
     *
     * @param player The player
     * @return How many shops they own
     */
    final public int countLocations(SignShopPlayer player) {
        int count = 0;
        for (Map.Entry<Location, Seller> entry : getSellerMap().entrySet())
            if (entry.getValue().isOwner(player)) {
                Block bSign = Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey());
                if (itemUtil.clickedSign(bSign)) {
                    String[] sLines = ((Sign) bSign.getState()).getLines();
                    List<String> operation = SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
                    if (operation.isEmpty())
                        continue;
                    // Not isOP. No need to count OP signs here because admins aren't really their owner
                    if (!operation.contains("playerIsOp"))
                        count++;
                }
            }
        return count;
    }

    /**
     * Get all signs using a certain block as a container
     *
     * @param bHolder The container
     * @return A list of signs using that block
     */
    final public List<Block> getSignsFromHolder(Block bHolder) {
        List<Block> signs = new LinkedList<>();
        for (Map.Entry<Location, Seller> entry : getSellerMap().entrySet())
            if (entry.getValue().getContainables().contains(bHolder))
                signs.add(Bukkit.getServer().getWorld(entry.getValue().getWorld()).getBlockAt(entry.getKey()));
        return signs;
    }

    /**
     * Get shops using a certain block as an activatable or container
     *
     * @param bBlock The block
     * @return A list of signs using that block
     */
    final public List<Seller> getShopsByBlock(Block bBlock) {
        List<Seller> tempsellers = new LinkedList<>();
        for (Map.Entry<Location, Seller> entry : getSellerMap().entrySet())
            if (entry.getValue().getActivatables().contains(bBlock) || entry.getValue().getContainables().contains(bBlock))
                tempsellers.add(entry.getValue());
        return tempsellers;
    }

    /**
     * Get a shop based on a special property
     *
     * @param key   Key name
     * @param value Value
     * @return List of shops matching those properties
     */
    final public List<Block> getShopsWithMiscSetting(String key, String value) {
        List<Block> shops = new LinkedList<>();
        for (Map.Entry<Location, Seller> entry : getSellerMap().entrySet()) {
            if (entry.getValue().hasMisc(key)) {
                if (entry.getValue().getMisc(key).contains(value))
                    shops.add(entry.getKey().getBlock());
            }
        }
        return shops;
    }

    /*
    Compatibility stuff and Initialization
     */

    private static final SignShopLogger logger = SignShopLogger.getStorageLogger();
    private static final SignShopLogger migrationLogger = SignShopLogger.getLogger("Storage Migrator");

    /**
     * The active storage implementation
     */
    private static Storage source;

    /**
     * The internal database
     */
    private static DatabaseDataSource database;

    /**
     * Initialize data storage using the implementation defined by {@link SignShopConfig#getDataSource()}
     * <br>If Storage has already been initialized, the existing Storage will be Saved and disposed of before the new Storage is initialized
     */
    public static void init() {
        DataSourceType type = SignShopConfig.getDataSource();

        logger.info("Setting Storage implementation to " + type.name());

        if (source != null) {
            logger.info("Saving and disposing of existing Storage: " + source.getClass().getSimpleName());

            source.saveSellers();
            source.dispose();
        }

        try {
            // Attempt to disabled logging by Hibernate/Hikari/MariaDB
            Configurator.setLevel("org.hibernate", Level.OFF);
            Configurator.setLevel("org.mariadb", Level.OFF);
            Configurator.setLevel("com.zaxxer.hikari", Level.OFF);
            if (SignShopConfig.debugging()) Configurator.setLevel("org.hibernate.SQL", Level.INFO);
        } catch (Exception ignored) {
        }

        // Initialize the internal database

        database = new DatabaseDataSource(type);

        if (type == DataSourceType.YML) {
            source = new YMLDataSource();
        } else {
            source = database;
        }

        source.loadSellers();

        if (!instanceValid(database)) {
            logger.error("Loading succeeded with errors...");
        }

        logger.info("Success! Set Storage implementation to " + type.name());
    }

    public static void onDisable() {
        logger.info("Saving session information and seller data");

        database.saveSchema();
        source.saveSellers();

        database.dispose();
        if (source != database) source.dispose();
    }

    /**
     * Get the active storage implementation
     *
     * @return The active storage implementation
     */
    public static Storage get() {
        return source;
    }

    public static InternalDatabase getBuiltInDatabase() {
        return database;
    }

    /**
     * Check that this instance of valid according to a {@link SignShopSchema}
     *
     * @param database The Database holding the Schema to validate against
     */
    public static boolean instanceValid(InternalDatabase database) {
        SignShopSchema schema = database.getSchema();

        if (!schema.needsStorageMigration() && !schema.needsDatabaseMigration()) return true;

        DataSourceType from = schema.getDataSource();
        DataSourceType to = source.getType();

        if (schema.needsDatabaseMigration()) {
            String fromUrl = schema.getConfig().getJdbcUrl();
            String toUrl = DatabaseUtil.getConfig().getJdbcUrl();
            migrationLogger.info(String.format("Migrating data from %s (%s) to %s (%s)", from.name(), fromUrl, to.name(), toUrl));
        } else if (schema.needsStorageMigration()) {
            migrationLogger.info(String.format("Migrating data from %s to %s", from.name(), to.name()));
        }

        Storage previousInstance;

        if (from == DataSourceType.YML) {
            previousInstance = new YMLDataSource();
        } else {
            // Migrating from one of SQLITE, MYSQL, MARIADB
            previousInstance = new DatabaseDataSource(from, schema.getConfig());
        }

        if (migrate(previousInstance)) {
            migrationLogger.info("Success!");
            schema.setDataSource(source.getType());
            if (to.isExternal()) schema.setConfig(DatabaseUtil.getConfig(true));
            database.saveSchema();
            return true;
        } else {
            migrationLogger.error("Failed to migrate data!");
            migrationLogger.info("   Saving migration data to database.migration... Please migrate the database yourself, or try the '/signshop MigrateDatabase' command");

            try {
                File migrationData = new File(SignShop.getInstance().getDataFolder(), String.format("database_%d.migration", System.currentTimeMillis()));

                // Write the current database configuration to file
                BufferedWriter fs = new BufferedWriter(new FileWriter(migrationData));
                fs.write("##########################################");
                fs.write("#             Migration Data             #");
                fs.write("##########################################");
                fs.write(HikariConfigConverter.configToString(schema.getConfig()).replace(";", "\n"));
                fs.close();
            } catch (Exception e) {
                migrationLogger.exception(e, "Failed to write migration file!");
            }
            return false;
        }
    }

    /**
     * Migrate data from another storage implementation to the current one
     *
     * @param oldStorage The old Storage
     * @return If the migration was successful
     */
    public static boolean migrate(Storage oldStorage) {
        oldStorage.loadSellers();

        // YML doesn't correct the data until the world is loaded, so make sure everything there
        if (oldStorage instanceof YMLDataSource) {
            try {
                for (World world : Bukkit.getWorlds()) {
                    WorldLoadEvent event = new WorldLoadEvent(world);
                    Bukkit.getPluginManager().callEvent(event);
                }
            } catch (Exception ignored) {
                migrationLogger.debug("Could not load worlds. Some shops may not have been migrated...");
            }
        }

        migrationLogger.info(String.format("Loaded %d shops from %s", oldStorage.shopCount(), oldStorage.getType().name()));

        int before = source.shopCount();

        // Move everything over
        source.getSellerMap().putAll(oldStorage.getSellerMap());

        int after = source.shopCount() - before;

        // Make sure it worked
        if (after != oldStorage.shopCount()) {
            migrationLogger.error(String.format("%d shops failed to migrate from %s!", oldStorage.shopCount() - after, source.getType().name()));
            return false;
        }

        migrationLogger.info(String.format("Saving %d shops from previous Storage", oldStorage.shopCount()));

        // Save
        source.saveSellers();

        return true;
    }

    /**
     * LEGACY: String seperator for legacy stored data
     *
     * @return The string seperator
     */
    public static String getItemSeperator() {
        return "&";
    }
}
