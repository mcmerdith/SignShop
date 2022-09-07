package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.bukkit.inventory.ItemStack;

public interface SSAttributeConverter<J, S> {
    S convertToDatabaseColumn(J itemStack);
    J convertToEntityAttribute(S serializable);
}
