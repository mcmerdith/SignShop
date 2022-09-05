package org.wargamer2010.signshop.configuration.storage.database.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.hibernate.engine.jdbc.BlobProxy;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.wargamer2010.signshop.configuration.storage.database.util.LazyLocation;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.*;

@Converter
public class ItemStackConverter implements AttributeConverter<ItemStack, Blob> {
    @Override
    public Blob convertToDatabaseColumn(ItemStack itemStack) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(output)) {
            if (itemStack != null) dataOutput.writeObject(itemStack);
            return BlobProxy.generateProxy(output.toByteArray());
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to serialize ItemStack!");
        }

        return null;
    }

    @Override
    public ItemStack convertToEntityAttribute(Blob serializable) {
        if (serializable == null)
            return null;

        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(serializable.getBinaryStream())) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to deserialize ItemStack!");
        }

        return null;
    }
}

