package org.wargamer2010.signshop.configuration.storage.database.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.configuration.storage.database.util.LazyLocation;

@Converter
public class LazyLocationConverter implements AttributeConverter<LazyLocation, String> {
    @Override
    public String convertToDatabaseColumn(LazyLocation lazy) {
        if (lazy == null) return null;
        return lazy.getStringRepresentation();
    }

    @Override
    public LazyLocation convertToEntityAttribute(String stringRepresentation) {
        if (stringRepresentation == null) return null;
        return new LazyLocation(stringRepresentation);
    }
}
