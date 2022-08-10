package org.wargamer2010.signshop.configuration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.PlayerIdentifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Storage {

    public Storage() {}

    abstract public boolean load();
    abstract public boolean save();
    abstract public void dispose();
    abstract public int shopCount();
    public void addSeller(@Nonnull PlayerIdentifier playerId, @Nonnull String sWorld, @Nonnull Block bSign, @Nonnull List<Block> containables, @Nonnull List<Block> activatables, @Nonnull ItemStack[] isItems, @Nonnull Map<String, String> misc) {
        addSeller(playerId, sWorld, bSign, containables, activatables, isItems, misc, true);
    }
    abstract public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save);
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables) {
        updateSeller(bSign, containables, activatables, null);
    }
    abstract public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems);
    abstract public Seller getSeller();
    abstract public Collection<Seller> getSellers();
    abstract public Block getSignFromSeller();
    abstract public void removeSeller();
    abstract public Integer countLocations();
    abstract public List<Block> getSignsFromHolder();
    abstract public List<Seller> getShopsByBlock();
    abstract public List<Block> getShopsWithMiscSetting();
    abstract public String getItemSeperator();
}
