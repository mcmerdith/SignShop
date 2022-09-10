package org.wargamer2010.signshop.configuration.orm;

import org.wargamer2010.signshop.configuration.orm.annotations.*;
import org.wargamer2010.signshop.configuration.orm.typing.SqlDialect;
import org.wargamer2010.signshop.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelData<T> extends ConcurrentHashMap<String, Object> {
    /**
     * The name of the table this model belongs to
     */
    private String tableName = null;

    /**
     * The SQL definitions for the columns
     * Mapped as: columnName -> columnDefinition
     */
    private final Map<String, ColumnData> columnDefinitions = new ConcurrentHashMap<>();
    private final Map<String, String> additionalConstraints = new ConcurrentHashMap<>();

    public ModelData(T model, SqlDialect dialect) {
        processAnnotatedModel(model.getClass(), dialect);
    }

    public ModelData(Class<T> modelClass, SqlDialect dialect) {
        processAnnotatedModel(modelClass, dialect);
    }

    /**
     * Read the @Model annotation (if present) on the Model class
     * @param annotated The model class
     */
    private void processAnnotatedModel(Class<?> annotated, SqlDialect dialect) {
        // Check for a @Model annotation. Not required, but does change the table name
        Model modelMeta = annotated.getDeclaredAnnotation(Model.class);

        if (modelMeta != null) {
            // Get the name from the annotation
            tableName = modelMeta.tableName();
        }

        if (StringUtils.isBlank(tableName)) {
            // Use the class name if we didn't define a name
            tableName = annotated.getSimpleName().toLowerCase();
        }

        for (Field field : annotated.getDeclaredFields()) {
            processAnnotatedField(field, dialect);
        }
    }

    /**
     * Read the annotations on a field
     * @param annotated The annotated field
     */
    private void processAnnotatedField(Field annotated, SqlDialect dialect) {
        ColumnData data = new ColumnData(annotated, dialect);

    }
}
