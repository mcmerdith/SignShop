package org.wargamer2010.signshop.configuration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.DatabaseDataSource;
import org.wargamer2010.signshop.configuration.database.InternalDatabase;
import org.wargamer2010.signshop.configuration.database.models.SignShopSchema;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SignShopLogger;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class Storage {
    /**
     * Load SignShops Data
     *
     * @return If the load was successful
     */
    abstract public boolean loadSellers();

    /**
     * Renamed to {@link Storage#loadSellers()}
     * @deprecated Will be removed at a later date
     */
    @Deprecated
    final public boolean Load() {
        return loadSellers();
    }

    /**
     * Save SignShops Data
     *
     * @return If the save was successful
     */
    abstract public boolean saveSellers();

    /**
     * Renamed to {@link Storage#saveSellers()}
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
    public void addSeller(@Nonnull PlayerIdentifier playerId, @Nonnull String sWorld, @Nonnull Block bSign, @Nonnull List<Block> containables, @Nonnull List<Block> activatables, @Nonnull ItemStack[] isItems, @Nonnull Map<String, String> misc) {
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
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables) {
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
    public Block getSignFromSeller(Seller pSeller) {
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
     * Move all of this Storage's data to a new Storage
     * @param newStorage The new Storage
     * @return If the migration was successful
     */
    abstract public boolean migrateTo(Storage newStorage);

    /*
    Compatibility stuff and Initialization
     */

    private static SignShopLogger logger = SignShopLogger.getStorageLogger();

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
            SignShopLogger migrationLogger = new SignShopLogger("Storage Migrator");

            SignShopConfig.DataSourceType from = database.getSchema().getDataSource();
            SignShopConfig.DataSourceType to = SignShopConfig.getDataSource();

            migrationLogger.info("Migrating data from " + from.name() + " to " + to.name());
            migrationLogger.info("Complete!");
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

    public static InternalDatabase getBuiltInDatabase() { return database; }

    /**
     * LEGACY: String seperator for legacy stored data
     *
     * @return The string seperator
     */
    public static String getItemSeperator() {
        return "&";
    }
}
