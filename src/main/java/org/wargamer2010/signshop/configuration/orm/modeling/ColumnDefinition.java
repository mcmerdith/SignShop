package org.wargamer2010.signshop.configuration.orm.modeling;

import org.wargamer2010.signshop.configuration.orm.NameManager;
import org.wargamer2010.signshop.configuration.orm.annotations.*;
import org.wargamer2010.signshop.configuration.orm.typing.ColumnType;
import org.wargamer2010.signshop.configuration.orm.typing.SqlType;
import org.wargamer2010.signshop.configuration.orm.typing.SqlTypeMapper;
import org.wargamer2010.signshop.configuration.orm.typing.conversion.SSAttributeConverter;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.StringUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ColumnDefinition {
    /*
    Column Definition
     */

    private String columnName = null;

    private ColumnType annotationColumnType = null;
    private String customColumnDefinition = "";

    private String defaultColumnValue = "";

    /*
    Constraints
     */

    private boolean primary = false;
    private boolean autoIncrement = false;

    private boolean nullable = true;

    private boolean unique = false;

    private boolean foreign = false;

    private String foreignReferencedTable = null;
    private String foreignReferencedColumn = null;

    private ColumnType foreignReferencedColumnDefinition = null;

    private String checkConstraint = null;

    /*
    Properties
     */

    private final boolean isTransient;

    private EnumType.Mode enumStorageMode = EnumType.Mode.VALUE;

    /*
    Arrays, Collections
     */

    private boolean array = false;

    private boolean collection = false;
    private Class<?> collectionType = null;

    private String collectionAssociatedTable = null;
    private String collectionReferenceColumnName = null;
    private String collectionValueColumnName = null;

    /*
    Data converters
     */
    private final List<SSAttributeConverter<?, ?>> converters = new ArrayList<>();
    private Class<?> convertedType = null;

    /*
    Objects used to build this column
     */
    private final ModelDefinition<?> model;
    private final Field annotatedField;

    public ColumnDefinition(ModelDefinition<?> model, Field annotatedField) {
        this.model = model;
        this.annotatedField = annotatedField;

        Transient transientAnnotation = annotatedField.getDeclaredAnnotation(Transient.class);

        // Transient fields are not tracked by the database
        isTransient = transientAnnotation != null;
        if (isTransient) return;

        // Check if the field is numerous, if so, extract the type
        processElementCollectionAnnotation(annotatedField.getDeclaredAnnotation(ElementCollection.class));

        // Load any converters, they'll be needed to determine the datatype
        processConvertsAnnotation(annotatedField.getDeclaredAnnotation(Converts.class));

        // Determine the defined properties of the column
        // Name, Defined type, Custom definition, Nullable, Unique
        processColumnAnnotation(annotatedField.getDeclaredAnnotation(Column.class));

        // Check if it's a primary key
        processIdAnnotation(annotatedField.getDeclaredAnnotation(Id.class));

        // Check if it's a foreign key
        processForeignKeyAnnotation(annotatedField.getDeclaredAnnotation(ForeignKey.class));

        /*
        Properties
         */

        // Get the enum storage mode
        processEnumTypeAnnotation(annotatedField.getDeclaredAnnotation(EnumType.class));

        // Get the column default value
        processDefaultAnnotation(annotatedField.getDeclaredAnnotation(Default.class));
    }

    /*
    Util Functions
     */

    private void processColumnAnnotation(@Nullable Column column) {
        if (column == null) {
            columnName = annotatedField.getName().toLowerCase();
            return;
        }

        // Load column data
        columnName = column.name();
        annotationColumnType = new ColumnType.Builder()
                .setType(column.definition())
                .setSize(column.size())
                .setLength(column.length())
                .setPrecision(column.precision(), column.scale())
                .build();
        nullable = column.nullable();
        unique = column.unique();
        customColumnDefinition = column.customDefinition();
        checkConstraint = column.check();

        // Default name
        if (StringUtils.isBlank(columnName)) columnName = annotatedField.getName().toLowerCase();
    }

    /**
     * If field is annotated with {@link Convert} load the converters<br>
     * Trace the converters from start to finish linking their conversion types together as:
     * `implicit type -> c1 -> c2 ->  ... -> final type`
     * <p>Method will fail if the converter takes a different type than the previous converter returns (or the implicit type)</p>
     */
    private void processConvertsAnnotation(@Nullable Converts converts) {
        // Load converters
        if (converts == null || converts.value().length == 0) return;

        String currentTypeName = getFieldType().getTypeName();

        for (Convert convert : converts.value()) {
            // Determine the conversion types
            Type[] superTypes = convert.converter().getGenericInterfaces();

            String fromTypeName = null;
            String toTypeName = null;

            for (Type superType : superTypes) {
                if (!(superType instanceof ParameterizedType)) continue;

                try {
                    ParameterizedType generic = (ParameterizedType) superType;

                    // We're looking for an SSAttributeConverter
                    if (!generic.getRawType().getTypeName().equals(SSAttributeConverter.class.getTypeName()))
                        continue;

                    Type[] types = generic.getActualTypeArguments();
                    fromTypeName = types[0].getTypeName();
                    toTypeName = types[1].getTypeName();
                    break;
                } catch (Exception ignored) {
                }
            }

            if (fromTypeName == null || toTypeName == null) {
                annotationError(convert, "could not determine types to convert");

                converters.clear();

                return;
            }

            if (fromTypeName.equals(currentTypeName)) {
                currentTypeName = toTypeName;
            } else {
                annotationError(convert, String.format("`%s` -> `%s` is not applicable for type `%s`%s",
                        fromTypeName,
                        toTypeName,
                        currentTypeName,
                        converts.value().length > 1 ? " (are they in order?)" : ""));

                converters.clear();

                return;
            }

            try {
                converters.add(convert.converter().getConstructor().newInstance());
            } catch (Exception e) {
                // Log the error
                annotationError(convert, String.format("was unable to instantiate converter for `%s` -> `%s`",
                        fromTypeName,
                        toTypeName));

                converters.clear();

                return;
            }
        }

        try {
            convertedType = Class.forName(currentTypeName);
        } catch (Exception e) {
            annotationError(converts, "encountered reflection error determining final type: " + e.getMessage());

            converters.clear();
        }
    }

    private void processEnumTypeAnnotation(@Nullable EnumType enumType) {
        if (enumType == null) return;

        this.enumStorageMode = enumType.mode();
    }

    private void processIdAnnotation(@Nullable Id id) {
        if (id == null) return;

        primary = true;

        if (id.autoIncrement()) {
            if (!getColumnType().type.incrementable() && customColumnDefinition == null) {

                // Make sure we can actually auto-increment it
                // If the user defined a type we can't validate it, so trust they know what they're doing
                autoIncrement = true;
            } else {
                annotationError(id, "autoincrement can only be applied to numeric fields!");
            }
        }
    }

    private void processElementCollectionAnnotation(@Nullable ElementCollection elementCollection) {
        if (elementCollection == null) return;

        array = annotatedField.getType().isArray();
        collection = Collection.class.isAssignableFrom(annotatedField.getType());

        if (!array && !collection) {
            annotationError(elementCollection, "can only be applied to Array or Collection fields");
            return;
        }

        if (foreign) {
            annotationError(elementCollection, "is not compatible with @ForeignKey");

            array = false;
            collection = false;

            return;
        }

        if (array) {
            collectionType = annotatedField.getType().getComponentType();
        }

        if (collection) {
            Type fType = annotatedField.getGenericType();

            if (fType instanceof ParameterizedType) {
                try {
                    Type[] fGenerics = ((ParameterizedType) fType).getActualTypeArguments();
                    collectionType = Class.forName(fGenerics[0].getTypeName());
                } catch (Exception e) {
                    annotationError(elementCollection, "could not load type: " + e.getMessage());

                    array = false;
                    collection = false;

                    return;
                }
            } else {
                annotationError(elementCollection, "could not determine type: raw use of generic Collection");

                array = false;
                collection = false;

                return;
            }
        }

        collectionAssociatedTable = elementCollection.associatedTableName();
        collectionReferenceColumnName = elementCollection.referenceColumnName();
        collectionValueColumnName = elementCollection.valueColumnName();

        if (StringUtils.isBlank(collectionAssociatedTable)) {
            collectionAssociatedTable = model.getTableName(true) + "_" + columnName;
        }

        if (StringUtils.isBlank(collectionReferenceColumnName)) {
            collectionReferenceColumnName = model.getTableName(true) + "_id";
        }

        if (StringUtils.isBlank(collectionValueColumnName)) {
            collectionValueColumnName = columnName;
        }
    }

    private void processForeignKeyAnnotation(@Nullable ForeignKey foreignKey) {
        if (foreignKey == null) return;
        if (array || collection) {
            annotationError(foreignKey, "is not compatible with @ElementCollcetion");
            // TODO allow list of ForeignKeys?
            return;
        }

        // If the user didn't define an association column
        // and the model doesn't have a unique key (primary or unique)
        ColumnDefinition foreignReferenceColumn = ModelManager.instance().getMapping(getFieldType()).getUniqueIdentifier();

        if (StringUtils.isBlank(foreignKey.associatedColumnName())
                && foreignReferenceColumn == null) {
            // We cannot assume the column to reference by
            annotationError(foreignKey, "cannot assume reference column on a table with no primary or unique keys");
            return;

        }

        foreign = true;
        foreignReferencedTable = foreignKey.associatedTableName();
        foreignReferencedColumn = foreignKey.associatedColumnName();
        foreignReferencedColumnDefinition = foreignReferenceColumn.getColumnType();

        if (StringUtils.isBlank(foreignReferencedTable)) {
            foreignReferencedTable = ModelManager.instance().getMapping(annotatedField.getType()).getTableName();
        }

        if (StringUtils.isBlank(foreignReferencedColumn)) {
            foreignReferencedColumn = ModelManager.instance().getMapping(getFieldType()).getUniqueIdentifier().getName();
        }
    }

    private void processDefaultAnnotation(Default defaultAnnotation) {
        if (defaultAnnotation == null) return;

        this.defaultColumnValue = defaultAnnotation.value();
    }

    private void annotationError(Annotation annotation, String error) {
        SignShopLogger.getLogger("Model").error(
                String.format(
                        "%s annotation cannot be applied to column `%s`. %s %s",
                        annotation.annotationType().getSimpleName(),
                        columnName,
                        annotation.annotationType().getSimpleName(),
                        error)
        );
    }

    /**
     * Get the final type of this field, after collection unwrapping (if needed) and type conversion (if needed)
     *
     * @return The final type of this field that will be mapped to the database
     */
    private Class<?> getFieldType() {
        if (convertedType != null) return convertedType;
        if (collectionType != null) return collectionType;
        return annotatedField.getType();
    }

    /**
     * Get the final column type.
     *
     * @return The defined type, foreign column type if present, else the type implied by the field
     * (converted and collection unwrapped if needed), else null
     */
    private ColumnType getColumnType() {
        if (annotationColumnType != null && annotationColumnType.type != SqlType.AUTO) return annotationColumnType;
        if (foreignReferencedColumnDefinition != null) return foreignReferencedColumnDefinition;
        return SqlTypeMapper.javaToSqlType(getFieldType());
    }

    /*
     * Getters
     */

    /**
     * Get the name of this column
     */
    public String getName() {
        return getName(false);
    }

    protected String getName(boolean raw) {
        return raw ? columnName : NameManager.instance().applyStrategiesForTable(columnName);
    }

    /**
     * Get the SQL definition for this column
     */
    public String getDefinition() {
        return getDefinition(true);
    }

    public String getDefinition(boolean includeConstraints) {
        // TODO deal with
        StringBuilder definition = new StringBuilder();

        // Add the column name
        definition.append(getName()).append(" ");

        // Get this columns type definition
        if (StringUtils.isBlank(customColumnDefinition)) {
            definition.append(getColumnType().getDefinition(ModelManager.instance().getDialect()));
        } else {
            definition.append(customColumnDefinition);
        }

        // If we don't need the constraints return
        if (!includeConstraints) return definition.toString();

        // Add the constraints
        if (!isNullable()) definition.append(" NOT NULL");
        if (autoIncrement) definition.append(" AUTO_INCREMENT");

        return definition.toString();
    }

    public List<SqlConstraint> getConstraints() {
        List<SqlConstraint> constraints = new ArrayList<>();

        if (primary) constraints.add(
                new SqlConstraint(model.getTableName(true), SqlConstraint.Type.PRIMARY_KEY, "(" + getName() + ")")
        );

        if (unique) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.UNIQUE, "(" + getName() + ")")
        );

        if (foreign) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.FOREIGN, "(" + getName() + ") REFERENCES " + getForeignReference())
        );

        if (!StringUtils.isBlank(checkConstraint)) constraints.add(
                new SqlConstraint(getName(), SqlConstraint.Type.CHECK, "(" + checkConstraint + ")")
        );

        return Collections.unmodifiableList(constraints);
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isForeignKey() {
        return foreign;
    }

    public String getForeignReference() {
        return String.format("%s(%s)", foreignReferencedTable, foreignReferencedColumn);
    }

    public boolean isTransient() {
        return isTransient;
    }

    public EnumType.Mode getEnumStorageMode() {
        return enumStorageMode;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isCollection() {
        return collection;
    }

    public String getCollectionAssociatedTable() {
        return collectionAssociatedTable;
    }

    public String getCollectionReferenceColumnName() {
        return collectionReferenceColumnName;
    }

    public String getCollectionValueColumnName() {
        return collectionValueColumnName;
    }

    public List<SSAttributeConverter<?, ?>> getConverters() {
        return converters;
    }

    public Field getField() {
        return this.annotatedField;
    }

    public ModelDefinition<?> getModel() {
        return model;
    }
}
