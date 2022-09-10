package org.wargamer2010.signshop.configuration.orm.modeling;

import org.wargamer2010.signshop.configuration.orm.typing.SqlDialect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelManager {
    private static ModelManager _instance;

    public static ModelManager instance() {
        if (_instance == null) _instance = new ModelManager();
        return _instance;
    }

    private ModelManager() {
        // Don't initialize this class
    }

    /*
    Manage creating/saving/updating/removing models
     */

    private SqlDialect dialect = SqlDialect.GENERIC;

    private final Map<Class<?>, ModelDefinition<?>> modelMappings = new ConcurrentHashMap<>();

    public void setDialect(SqlDialect dialect) {
        // Don't change the dialect unnecessarily
        if (this.dialect == dialect) return;

        this.dialect = dialect;

        // We need to rebuild the model mappings after changing dialects
        modelMappings.clear();
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    /**
     * Register a model class with this manager
     * Class and declared fields will be traversed for annotations from the
     * {@link org.wargamer2010.signshop.configuration.orm.annotations} package
     * @param model The model class to be registered
     */
    public void registerModel(Class<?> model) {
        modelMappings.put(model, new ModelDefinition<>(model));
    }

    /**
     * Get an {@link ModelDefinition} associated with this class
     * @param model The class to get the model of
     * @return An {@link ModelDefinition} of the class
     * @param <T> The class that the model will be made more
     */
    @SuppressWarnings("unchecked")
    public <T> ModelDefinition<T> getMapping(Class<T> model) {
        if (!modelMappings.containsKey(model)) registerModel(model);
        return (ModelDefinition<T>) modelMappings.get(model);
    }

    private String generateCreation(ModelDefinition<?> model) {
        return null;
    }

    private String generateDropping(ModelDefinition<?> model) {
        return null;
    }

    private <T> String generateUpdate(ModelDefinition<T> model, T data) {
        return null;
    }

    private <T> String generateInsertion(ModelDefinition<T> model, T data) {
        return null;
    }
}
