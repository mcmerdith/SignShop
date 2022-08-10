package org.wargamer2010.signshop.configuration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DatabaseDataSource extends Storage {
    public DatabaseDataSource() {
        super();
    }

    @Override
    public boolean Load() {
        return false;
    }

    @Override
    public boolean Save() {
        return false;
    }

    @Override
    public void dispose() {

    }

    @Override
    public int shopCount() {
        return 0;
    }

    @Override
    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save) {

    }

    @Override
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems) {

    }

    @Override
    public Seller getSeller(Location lKey) {
        return null;
    }

    @Override
    public Collection<Seller> getSellers() {
        return null;
    }

    @Override
    public void removeSeller(Location lKey) {

    }

    @Override
    public int countLocations(SignShopPlayer player) {
        return 0;
    }

    @Override
    public List<Block> getSignsFromHolder(Block bHolder) {
        return null;
    }

    @Override
    public List<Seller> getShopsByBlock(Block bBlock) {
        return null;
    }

    @Override
    public List<Block> getShopsWithMiscSetting(String key, String value) {
        return null;
    }
}
