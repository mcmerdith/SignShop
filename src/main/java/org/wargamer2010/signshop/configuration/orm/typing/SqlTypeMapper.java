package org.wargamer2010.signshop.configuration.orm.typing;

import java.util.HashMap;
import java.util.Map;

/**
 * Map Java types to SQL types and vice versa
 */
public class SqlTypeMapper {

    private static final Map<Class<?>, ColumnType> equivalent = new HashMap<>();

    static {
        // Strings
        equivalent.put(String.class, new ColumnType(SqlType.STRING));

        // Primitives need both their wrapper and primitive class registered
        ColumnType TINYINT = new ColumnType(SqlType.INT, SqlType.Size.TINY);
        equivalent.put(Byte.class, TINYINT);
        equivalent.put(Byte.TYPE, TINYINT);

        ColumnType MEDIUMINT = new ColumnType(SqlType.INT, SqlType.Size.MEDIUM);
        equivalent.put(Short.class, MEDIUMINT);
        equivalent.put(Short.TYPE, MEDIUMINT);

        ColumnType INTEGER = new ColumnType(SqlType.INT);
        equivalent.put(Integer.class, INTEGER);
        equivalent.put(Integer.TYPE, INTEGER);

        ColumnType FLOAT = new ColumnType(SqlType.FLOAT);
        equivalent.put(Long.class, FLOAT);
        equivalent.put(Long.TYPE, FLOAT);

        equivalent.put(Double.class, FLOAT);
        equivalent.put(Double.TYPE, FLOAT);

        equivalent.put(Float.class, FLOAT);
        equivalent.put(Float.TYPE, FLOAT);

        equivalent.put(Boolean.class, FLOAT);
        equivalent.put(Boolean.TYPE, FLOAT);
    }

    /**
     * Get the SQL type best representing a Java object
     * @param java The object to get the type of
     * @return The SQL type, or null if a suitable match cannot be found
     */
    public static SqlType javaToSqlType(Object java) {
        return null;
    }
}
