package org.wargamer2010.signshop.configuration.orm.typing.conversion;

import org.wargamer2010.signshop.configuration.storage.database.util.LazyLocation;

public class LazyLocationConverter implements SSAttributeConverter<LazyLocation, String> {
    @Override
    public String convertToDatabaseColumn(LazyLocation lazy) {
        if (lazy == null) return null;
        return lazy.getStringRepresentation();
    }

    @Override
    public LazyLocation convertToModelAttribute(String stringRepresentation) {
        if (stringRepresentation == null) return null;
        return new LazyLocation(stringRepresentation);
    }
}
