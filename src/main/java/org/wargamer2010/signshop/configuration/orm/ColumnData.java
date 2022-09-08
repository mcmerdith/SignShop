package org.wargamer2010.signshop.configuration.orm;

import org.wargamer2010.signshop.configuration.orm.annotations.*;
import org.wargamer2010.signshop.configuration.orm.typemapping.ColumnType;
import org.wargamer2010.signshop.configuration.orm.typemapping.SqlDialect;
import org.wargamer2010.signshop.configuration.orm.typemapping.conversion.SSAttributeConverter;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColumnData {

    private String columnName = null;
    private String defaultDefinition = null;
    private String customDefinition = "";
    private final List<String> constraints = new ArrayList<>();

    private boolean nullable = true;
    private boolean unique = false;

    private final boolean isTransient;

    private final List<SSAttributeConverter<?, ?>> converters = new ArrayList<>();

    public ColumnData(Field annotated, SqlDialect dialect) {

        Transient transientAnnotation = annotated.getDeclaredAnnotation(Transient.class);

        // Transient fields are not tracked by the database
        isTransient = transientAnnotation != null;
        if (isTransient) return;

        Column column = annotated.getDeclaredAnnotation(Column.class);

        // Temp variables
        ColumnType.Builder typeBuilder = new ColumnType.Builder();

        // Load column data
        if (column != null) {
            columnName = column.name();
            typeBuilder.setType(column.definition());
            typeBuilder.setSize(column.size());
            typeBuilder.setLength(column.length());
            typeBuilder.setPrecision(column.precision(), column.scale());
            nullable = column.nullable();
            unique = column.unique();
            customDefinition = column.customDefinition();
        }

        // Default name
        if (StringUtils.isBlank(columnName)) columnName = annotated.getName();

        // Default definition
        defaultDefinition = typeBuilder.build().getSql(dialect);

        Converts converts = annotated.getDeclaredAnnotation(Converts.class);

        // Load converters
        if (converts != null) {
            for (Convert convert : converts.value()) {
                try {
                    converters.add(convert.converter().getConstructor().newInstance());
                } catch (Exception e) {
                    String fromName = "Unknown Type";
                    String toName = "Unknown Type";
                    Type[] superTypes = convert.converter().getGenericInterfaces();
                    for (Type superType : superTypes) {
                        if (!(superType instanceof ParameterizedType)) continue;

                        try {
                            // Try to read the types from the generic
                            ParameterizedType generic = (ParameterizedType) superType;

                            // We're looking for an SSAttributeConverter
                            if (!generic.getRawType().getTypeName().equals(SSAttributeConverter.class.getTypeName())) continue;

                            Type[] types = generic.getActualTypeArguments();
                            fromName = types[0].getTypeName();
                            toName = types[1].getTypeName();
                        } catch (Exception ignored) {
                        }
                    }

                    SignShopLogger.getDatabaseLogger().exception(e,
                            String.format("Could not initialize converter for column `%s` from type `%s` to `%s`",
                                    columnName,
                                    fromName,
                                    toName));
                }
            }
        }

        EnumType enumType = annotated.getDeclaredAnnotation(EnumType.class);
        Id id = annotated.getDeclaredAnnotation(Id.class);
        ManyToOne manyToOne = annotated.getDeclaredAnnotation(ManyToOne.class);
        OneToOne oneToOne = annotated.getDeclaredAnnotation(OneToOne.class);
    }

    /**
     * Get the name of this column
     */
    public String getName() {
        return columnName;
    }

    /**
     * Get the SQL definition for this column
     */
    public String getDefinition() {
        if (StringUtils.isBlank(customDefinition)) {
            return defaultDefinition;
        } else {
            return customDefinition;
        }
    }

    public List<String> getAdditionalConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }
}
