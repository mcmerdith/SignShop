package org.wargamer2010.signshop.configuration.orm.modeling;

import org.apache.commons.lang.NotImplementedException;
import org.wargamer2010.signshop.configuration.orm.NameManager;
import org.wargamer2010.signshop.configuration.orm.annotations.*;
import org.wargamer2010.signshop.util.SignShopLogger;
import org.wargamer2010.signshop.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelDefinition<T> extends ConcurrentHashMap<String, Object> {
    /**
     * The name of the table this model belongs to
     */
    private String tableName = null;

    /**
     * The SQL definitions for the columns
     * Mapped as: columnName -> columnDefinition
     */
    private final Map<String, ColumnDefinition> columnDefinitions = new ConcurrentHashMap<>();

    public ModelDefinition(Class<T> modelClass) {
        // Check for a @Model annotation. Not required, but does change the table name
        Model modelMeta = modelClass.getDeclaredAnnotation(Model.class);

        if (modelMeta != null) {
            // Get the name from the annotation
            tableName = modelMeta.tableName();
        }

        if (StringUtils.isBlank(tableName)) {
            // Use the class name if we didn't define a name
            tableName = modelClass.getSimpleName().toLowerCase();
        }

        for (Field field : modelClass.getDeclaredFields()) {
            processAnnotatedField(field);
        }

        if (columnDefinitions.values().stream().filter(ColumnDefinition::isPrimary).count() > 1) {
            throw SignShopLogger.getLogger("Model").exception(null, "Model " + tableName + " has more than 1 primary key!", true);
        }
    }

    /**
     * Read the annotations on a field
     *
     * @param annotated The annotated field
     */
    private void processAnnotatedField(Field annotated) {
        ColumnDefinition column = new ColumnDefinition(this, annotated);

        // Don't track transient columns
        if (column.isTransient()) return;

        columnDefinitions.put(annotated.getName(), column);
    }

    public String getTableName() {
        return getTableName(false);
    }

    protected String getTableName(boolean raw) {
        return raw ? tableName : NameManager.instance().applyStrategiesForTable(tableName);
    }

    /**
     * Get the column that uniquely identifies this model
     *
     * @return The Primary Key (if present) or the most suitable Unique Key, else null
     */
    public ColumnDefinition getUniqueIdentifier() {
        columnDefinitions.values().stream().filter(ColumnDefinition::isPrimary).findAny();
        throw new NotImplementedException(); // TODO get the primary/unique column for this model
    }

    public Collection<ColumnDefinition> getColumnDefinitions() {
        return Collections.unmodifiableCollection(columnDefinitions.values().);
    }
}
