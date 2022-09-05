package org.wargamer2010.signshop.mocks;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

public class TestUnsafeValues implements UnsafeValues {
    private static TestUnsafeValues _instance;

    public static TestUnsafeValues instance() {
        if (_instance == null) _instance = new TestUnsafeValues();
        return _instance;
    }

    private TestUnsafeValues() {
        // Singleton constructor
    }

    @Override
    public Material toLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(MaterialData material) {
        return material.getItemType();
    }

    @Override
    public Material fromLegacy(MaterialData material, boolean itemPriority) {
        return material.getItemType();
    }

    @Override
    public BlockData fromLegacy(Material material, byte data) {
        return material.createBlockData();
    }

    @Override
    public int getDataVersion() {
        return 0;
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        return stack;
    }

    @Override
    public void checkSupported(PluginDescriptionFile pdf) throws InvalidPluginException {

    }

    @Override
    public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
        return new byte[0];
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey key) {
        return false;
    }
}
