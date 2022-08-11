package org.wargamer2010.signshop.configuration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Storage {


    public Storage() {}

    /**
     * Load SignShops Data
     * @return If the load was successful
     */
    abstract public boolean load();

    /**
     * Save SignShops Data
     * @return If the save was successful
     */
    abstract public boolean save();

    /**
     * Clean up any data before closing
     */
    abstract public void dispose();

    /**
     * How many shops are stored
     * @return
     */
    abstract public int shopCount();

    /**
     * Save a new shop
     * @param playerId Player owner
     * @param sWorld The world the shop is in
     * @param bSign The sign
     * @param containables TODO containables
     * @param activatables TODO activatables
     * @param isItems TODO isItems
     * @param misc TODO misc
     */
    public void addSeller(@Nonnull PlayerIdentifier playerId, @Nonnull String sWorld, @Nonnull Block bSign, @Nonnull List<Block> containables, @Nonnull List<Block> activatables, @Nonnull ItemStack[] isItems, @Nonnull Map<String, String> misc) {
        addSeller(playerId, sWorld, bSign, containables, activatables, isItems, misc, true);
    }

    /**
     * Add a new shop
     * @param playerId Player owner
     * @param sWorld The world the shop is in
     * @param bSign The sign
     * @param containables TODO containables
     * @param activatables TODO activatables
     * @param isItems TODO isItems
     * @param misc TODO misc
     * @param save If we should save
     */
    abstract public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save);

    /**
     * Update a shop
     * @param bSign The new sign
     * @param containables TODO containables
     * @param activatables TODO activatables
     */
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables) {
        updateSeller(bSign, containables, activatables, null);
    }

    /**
     * Update a shop
     * @param bSign The new sign
     * @param containables TODO containables
     * @param activatables TODO activatables
     * @param isItems TODO isItems
     */
    abstract public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems);

    /**
     * Get a seller
     * @param lKey The location of the sign
     * @return The sign, or null if not found
     */
    abstract public Seller getSeller(Location lKey);

    /**
     * Get all currently saved shops
     * @return An collection of shops
     */
    abstract public Collection<Seller> getSellers();

    /**
     * Get the sign associated with a shop
     * @param pSeller The shop
     * @return The sign
     * @deprecated Shops store their own sign location, this is only a legacy helper method
     */
    @Deprecated
    public Block getSignFromSeller(Seller pSeller) { return pSeller.getSign(); }

    /**
     * Delete a shop
     * @param lKey The location of the shop
     */
    abstract public void removeSeller(Location lKey);

    /**
     * Get how many shops are owned by a player
     * @param player The player
     * @return How many shops they own
     */
    abstract public int countLocations(SignShopPlayer player);

    /**
     * Get all signs using a certain container
     * @param bHolder The container
     * @return A list of signs using that container
     */
    abstract public List<Block> getSignsFromHolder(Block bHolder);

    /**
     * Get shops using a certain block as an activatable or container
     * @param bBlock The block
     * @return A list of signs using that block
     */
    abstract public List<Seller> getShopsByBlock(Block bBlock);

    /**
     * TODO what is misc?
     * @param key TODO key
     * @param value TODO value
     * @return TODO
     */
    abstract public List<Block> getShopsWithMiscSetting(String key, String value);
}
