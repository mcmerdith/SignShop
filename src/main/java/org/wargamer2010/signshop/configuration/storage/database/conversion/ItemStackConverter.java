package org.wargamer2010.signshop.configuration.storage.database.conversion;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.wargamer2010.signshop.configuration.orm.typing.SSAttributeConverter;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemStackConverter implements SSAttributeConverter<ItemStack, byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(ItemStack itemStack) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(output)) {
            if (itemStack != null) dataOutput.writeObject(itemStack);
            return output.toByteArray();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to serialize ItemStack!");
        }

        return null;
    }

    @Override
    public ItemStack convertToModelAttribute(byte[] serializable) {
        if (serializable == null)
            return null;

        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(new ByteArrayInputStream(serializable))) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to deserialize ItemStack!");
        }

        return null;
    }
}

