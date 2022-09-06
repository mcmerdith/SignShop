package org.wargamer2010.signshop.configuration.storage.database.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LazyLocation {
    private final String locationString;
    private Location location;

    /**
     * Lazy-initialize this location
     *
     * @param locationString The string representing the {@link Location} object
     */
    public LazyLocation(String locationString) {
        this.locationString = locationString;
    }

    /**
     * Immediately initialize this location
     *
     * @param location The Location
     */
    public LazyLocation(Location location) {
        this.location = location;
        this.locationString = signshopUtil.convertLocationToString(location);
    }

    /**
     * Immediately initialize this location
     *
     * @param block The Block
     */
    public LazyLocation(Block block) {
        this(block.getLocation());
    }

    /**
     * Get the string form of this location as defined by {@link signshopUtil#convertLocationToString(Location)}
     *
     * @return The location this object represents formatted as "x/y/z/world"
     */
    public String getStringRepresentation() {
        return this.locationString;
    }

    /**
     * Get the {@link Location} this object represents
     *
     * @return The location represented by this object
     */
    public Location get() {
        if (location == null) location = signshopUtil.convertStringToLocation(locationString, null);

        return location;
    }

    /**
     * Get the {@link Block} at the {@link Location} this object represents
     *
     * @return The block at the location represented by this object
     */
    public Block getBlock() {
        return get().getBlock();
    }

    /**
     * Unwrap a {@link Collection} of {@link LazyLocation}s
     *
     * @param collection The collection to unwrap
     * @return The unwrapped collection
     */
    public static List<Block> collectionOfToBlockList(Collection<LazyLocation> collection) {
        return collection.stream().map(LazyLocation::getBlock).collect(Collectors.toList());
    }

    /**
     * Wrap a {@link Collection} of {@link Block}s in {@link LazyLocation}s
     *
     * @param collection The collection to wrap
     * @return The wrapped collection
     */
    public static List<LazyLocation> collectionOfBlockToListOf(Collection<Block> collection) {
        return collection.stream().map(LazyLocation::new).collect(Collectors.toList());
    }

    /**
     * Unwrap a {@link Collection} of {@link LazyLocation}s
     *
     * @param collection The collection to unwrap
     * @return The unwrapped collection
     */
    public static List<Location> collectionOfToLocationList(Collection<LazyLocation> collection) {
        return collection.stream().map(LazyLocation::get).collect(Collectors.toList());
    }

    /**
     * Wrap a {@link Collection} of {@link Block}s in {@link LazyLocation}s
     *
     * @param collection The collection to wrap
     * @return The wrapped collection
     */
    public static List<LazyLocation> collectionOfLocationToListOf(Collection<Location> collection) {
        return collection.stream().map(LazyLocation::new).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LazyLocation)) return false;
        LazyLocation otherLocation = (LazyLocation) obj;

        return otherLocation.locationString.equals(locationString);
    }
}
