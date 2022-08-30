package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.itemUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ItemStackType implements UserType<ItemStack> {
    @Override
    public int getSqlType() {
        return Types.BLOB;
    }

    @Override
    public Class<ItemStack> returnedClass() {
        return ItemStack.class;
    }

    @Override
    public boolean equals(ItemStack itemStack, ItemStack j1) {
        return itemStack.equals(j1);
    }

    @Override
    public int hashCode(ItemStack itemStack) {
        return itemStack.hashCode();
    }

    @Override
    public ItemStack nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        return assemble(resultSet.getBytes(i), o);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, ItemStack itemStack, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
        preparedStatement.setBytes(i, (byte[]) disassemble(itemStack));
    }

    @Override
    public ItemStack deepCopy(ItemStack itemStack) {
        return itemUtil.getBackupSingleItemStack(itemStack);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(ItemStack itemStack) {
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
    public ItemStack assemble(Serializable serializable, Object o) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) serializable);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            if (serializable == null)
                throw new Exception("Cannot deserialize 'null'!");
            if (!(serializable instanceof byte[]))
                throw new Exception("Cannot decode serialized ItemStack! Expected 'byte[]' got " + serializable.getClass().getSimpleName());

            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to deserialize ItemStack!");
        }

        return null;
    }

    @Override
    public ItemStack replace(ItemStack itemStack, ItemStack j1, Object o) {
        return deepCopy(itemStack);
    }
}

