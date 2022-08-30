package org.wargamer2010.signshop.configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.storage.DatabaseDataSource;
import org.wargamer2010.signshop.configuration.storage.database.InternalDatabase;
import org.wargamer2010.signshop.configuration.storage.YMLDataSource;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SignShopLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Storage {
    /**
     * Load SignShops Data
     *
     * @return If the load was successful
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
     *
     * @return If the data needs saving
     */
    abstract public boolean saveSellers();

    /**
     * Renamed to {@link Storage#saveSellers()}
     *
     * @deprecated Will be removed at a later date
     */
    @Deprecated
    final public boolean Save() {
        return saveSellers();
    }

    /**
     * Clean up any data before closing
     */
    abstract public void dispose();

    /**
     * How many shops are stored
     *
     * @return
     */
    abstract public int shopCount();

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
    abstract public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems);

    /**
     * Get a seller
     *
     * @param lKey The location of the sign
     * @return The sign, or null if not found
     */
    abstract public Seller getSeller(Location lKey);

    /**
     * Get all currently saved shops
     *
     * @return An collection of shops
     */
    abstract public Collection<Seller> getSellers();

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
     * Delete a shop
     *
     * @param lKey The location of the shop
     */
    abstract public void removeSeller(Location lKey);

    /**
     * Get how many shops are owned by a player
     *
     * @param player The player
     * @return How many shops they own
     */
    abstract public int countLocations(SignShopPlayer player);

    /**
     * Get all signs using a certain container
     *
     * @param bHolder The container
     * @return A list of signs using that container
     */
    abstract public List<Block> getSignsFromHolder(Block bHolder);

    /**
     * Get shops using a certain block as an activatable or container
     *
     * @param bBlock The block
     * @return A list of signs using that block
     */
    abstract public List<Seller> getShopsByBlock(Block bBlock);

    /**
     * Get a shop based on a special property
     *
     * @param key   Key name
     * @param value Value
     * @return List of shops matching those properties
     */
    abstract public List<Block> getShopsWithMiscSetting(String key, String value);

    /**
     * Get the Storage implementation this DataSource represents
     * @return The implemented datasource
     */
    abstract public SignShopConfig.DataSourceType getType();

    /*
    Compatibility stuff and Initialization
     */

    private static final SignShopLogger logger = SignShopLogger.getStorageLogger();

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
        SignShopConfig.DataSourceType type = SignShopConfig.getDataSource();

        logger.info("Setting Storage implementation to " + type.name());

        if (source != null) {
            logger.info("Saving and disposing of existing Storage: " + source.getClass().getSimpleName());

            source.saveSellers();
            source.dispose();
        }

        // Initialize the internal database
        database = new DatabaseDataSource(type);

        switch (type) {
            case MARIADB:
            case MYSQL:
            case SQLITE:
                // Don't make more than 1 instance of the database, use the same one
                source = database;
                break;
            case YML:
                source = new YMLDataSource(new File(SignShop.getInstance().getDataFolder(), "sellers.yml"));
                break;
        }

        if (database.getSchema().needsStorageMigration()) {
            SignShopConfig.DataSourceType from = database.getSchema().getDataSource();
            SignShopConfig.DataSourceType to = SignShopConfig.getDataSource();
        }

        logger.info("Success!");
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
     * Migrate data from another storage implementation to the current on
     *
     * @param oldStorage The old Storage
     * @return If the migration was successful
     */
    public static boolean migrate(Storage oldStorage) {
        SignShopLogger migrationLogger = new SignShopLogger("Storage Migrator");

        SignShopConfig.DataSourceType oldType = oldStorage.getType();
        SignShopConfig.DataSourceType currentType = source.getType();

        migrationLogger.info("Migrating data from " + oldType.name() + " to " + currentType.name());

        if (oldType == currentType) {
            migrationLogger.info("Storage implementation has not changed! No need to migrate");
            return true;
        }

//        if (database.getSchema().needsVersionConversion()) {
//            migrationLogger.error("Cannot perform Storage migration when the database is out of data! Please revert your configuration and restart the server!");
//            return false;
//        }

        if (oldType != SignShopConfig.DataSourceType.YML && currentType != SignShopConfig.DataSourceType.YML
                && oldType != SignShopConfig.DataSourceType.SQLITE && currentType != SignShopConfig.DataSourceType.SQLITE) {
            migrationLogger.error("Unable to automatically migrate data between two external databases! Please use the `/signshop MigrateDatabase` command or migrate the database yourself.");

            return false;
        }

        oldStorage.loadSellers();

        // YML doesn't correct the data until the world is loaded, so make sure everything there
        if (oldStorage instanceof YMLDataSource) {
            try {
                for (World world : Bukkit.getWorlds()) {
                    WorldLoadEvent event = new WorldLoadEvent(world);
                    Bukkit.getPluginManager().callEvent(event);
                }
            } catch (Exception ignored) {
                migrationLogger.debug("Could not load worlds for YMLDataSource. Some shops may not have been loaded");
            }
        }

        migrationLogger.info("Complete!");

        return false;
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
