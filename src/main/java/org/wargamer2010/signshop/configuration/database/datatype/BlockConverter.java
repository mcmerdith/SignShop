package org.wargamer2010.signshop.configuration.database.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;

@Converter
public class BlockConverter implements AttributeConverter<Block, Location> {
    @Override
    public Location convertToDatabaseColumn(Block block) {
        if (block == null) return null;
        return block.getLocation();
    }

    @Override
    public Block convertToEntityAttribute(Location l) {
        if (l == null) return null;
        return l.getBlock();
    }
}
