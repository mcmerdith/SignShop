package org.wargamer2010.signshop.mocks;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestItemFactory implements ItemFactory {
    private static TestItemFactory _instance;

    public static TestItemFactory instance() {
        if (_instance == null) _instance = new TestItemFactory();
        return _instance;
    }

    private TestItemFactory() {
        // Singleton constructor
    }

    @Nullable
    @Override
    public ItemMeta getItemMeta(@NotNull Material material) {
        return null;
    }

    @Override
    public boolean isApplicable(@Nullable ItemMeta meta, @Nullable ItemStack stack) throws IllegalArgumentException {
        return true;
    }

    @Override
    public boolean isApplicable(@Nullable ItemMeta meta, @Nullable Material material) throws IllegalArgumentException {
        return true;
    }

    @Override
    public boolean equals(@Nullable ItemMeta meta1, @Nullable ItemMeta meta2) throws IllegalArgumentException {
        return (meta1 == null && meta2 == null) || (meta1 != null && meta1.equals(meta2));
    }

    @Nullable
    @Override
    public ItemMeta asMetaFor(@NotNull ItemMeta meta, @NotNull ItemStack stack) throws IllegalArgumentException {
        return meta;
    }

    @Nullable
    @Override
    public ItemMeta asMetaFor(@NotNull ItemMeta meta, @NotNull Material material) throws IllegalArgumentException {
        return meta;
    }

    @NotNull
    @Override
    public Color getDefaultLeatherColor() {
        return Color.BLACK;
    }

    @NotNull
    @Override
    public Material updateMaterial(@NotNull ItemMeta meta, @NotNull Material material) throws IllegalArgumentException {
        return material;
    }
}
