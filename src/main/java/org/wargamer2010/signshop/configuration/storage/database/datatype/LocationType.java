package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.wargamer2010.signshop.util.SignShopLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LocationType implements UserType<Location> {
    @Override
    public int getSqlType() {
        return Types.BLOB;
    }

    @Override
    public Class<Location> returnedClass() {
        return Location.class;
    }

    @Override
    public boolean equals(Location location, Location j1) {
        return location.equals(j1);
    }

    @Override
    public int hashCode(Location location) {
        return location.hashCode();
    }

    @Override
    public Location nullSafeGet(ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws SQLException {
        return assemble(resultSet.getBytes(i), o);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Location location, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
        preparedStatement.setBytes(i, (byte[]) disassemble(location));
    }

    @Override
    public Location deepCopy(Location location) {
        return location.clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Location location) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(output)) {
            if (location != null) dataOutput.writeObject(location);
            return output.toByteArray();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to serialize Location!");
        }

        return null;
    }

    @Override
    public Location assemble(Serializable serializable, Object o) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) serializable);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            if (serializable == null)
                throw new Exception("Cannot deserialize 'null'!");
            if (!(serializable instanceof byte[]))
                throw new Exception("Cannot decode serialized Location! Expected 'byte[]' got " + serializable.getClass().getSimpleName());

            return (Location) dataInput.readObject();
        } catch (Exception e) {
            SignShopLogger.getDatabaseLogger().exception(e, "Failed to deserialize Location!");
        }

        return null;
    }

    @Override
    public Location replace(Location location, Location j1, Object o) {
        return deepCopy(location);
    }
}

