package org.wargamer2010.signshop.mocks;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestBlock implements Block {
    private final Location location;
    private final Material type;
    private final List<ItemStack> drops = new ArrayList<>();

    public TestBlock(World world, int x, int y, int z, Material type, List<ItemStack> drops) {
        this(new Location(world, x, y, z), type, drops);
    }

    public TestBlock(Location location, Material type, List<ItemStack> drops) {
        this.location = location;
        this.type = type;
        if (drops != null) this.drops.addAll(drops);
        else this.drops.add(new ItemStack(this.type));

        if (this.location.getWorld() instanceof TestWorld) {
            ((TestWorld) this.location.getWorld()).registerBlock(this.location, this);
        }
    }

    @Override
    public byte getData() {
        return 0;
    }

    @NotNull
    @Override
    public BlockData getBlockData() {
        return null;
    }

    @NotNull
    @Override
    public Block getRelative(int modX, int modY, int modZ) {
        return this;
    }

    @NotNull
    @Override
    public Block getRelative(@NotNull BlockFace face) {
        return this;
    }

    @NotNull
    @Override
    public Block getRelative(@NotNull BlockFace face, int distance) {
        return this;
    }

    @NotNull
    @Override
    public Material getType() {
        return type;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public byte getLightFromSky() {
        return 0;
    }

    @Override
    public byte getLightFromBlocks() {
        return 0;
    }

    @NotNull
    @Override
    public World getWorld() {
        return getLocation().getWorld();
    }

    @Override
    public int getX() {
        return getLocation().getBlockX();
    }

    @Override
    public int getY() {
        return getLocation().getBlockY();
    }

    @Override
    public int getZ() {
        return getLocation().getBlockZ();
    }

    @NotNull
    @Override
    public Location getLocation() {
        return location;
    }

    @Nullable
    @Override
    public Location getLocation(@Nullable Location loc) {
        if (loc == null) return null;

        loc.setWorld(getWorld());
        loc.setX(getX());
        loc.setY(getY());
        loc.setZ(getZ());
        loc.setPitch(0);
        loc.setYaw(0);

        return loc;
    }

    @NotNull
    @Override
    public Chunk getChunk() {
        return null;
    }

    @Override
    public void setBlockData(@NotNull BlockData data) {

    }

    @Override
    public void setBlockData(@NotNull BlockData data, boolean applyPhysics) {

    }

    @Override
    public void setType(@NotNull Material type) {

    }

    @Override
    public void setType(@NotNull Material type, boolean applyPhysics) {

    }

    @Nullable
    @Override
    public BlockFace getFace(@NotNull Block block) {
        return null;
    }

    @NotNull
    @Override
    public BlockState getState() {
        return null;
    }

    @NotNull
    @Override
    public Biome getBiome() {
        return null;
    }

    @Override
    public void setBiome(@NotNull Biome bio) {

    }

    @Override
    public boolean isBlockPowered() {
        return false;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return false;
    }

    @Override
    public boolean isBlockFacePowered(@NotNull BlockFace face) {
        return false;
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(@NotNull BlockFace face) {
        return false;
    }

    @Override
    public int getBlockPower(@NotNull BlockFace face) {
        return 0;
    }

    @Override
    public int getBlockPower() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isLiquid() {
        return false;
    }

    @Override
    public double getTemperature() {
        return 0;
    }

    @Override
    public double getHumidity() {
        return 0;
    }

    @NotNull
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return PistonMoveReaction.MOVE;
    }

    @Override
    public boolean breakNaturally() {
        return true;
    }

    @Override
    public boolean breakNaturally(@NotNull ItemStack tool) {
        return true;
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops() {
        return drops;
    }

    @NotNull
    @Override
    public Collection<ItemStack> getDrops(@NotNull ItemStack tool) {
        return drops;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Nullable
    @Override
    public RayTraceResult rayTrace(@NotNull Location start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(0, 0, 0, 1, 1, 1);
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {

    }

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {

    }
}
