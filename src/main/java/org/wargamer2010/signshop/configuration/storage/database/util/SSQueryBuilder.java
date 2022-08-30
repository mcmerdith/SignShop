package org.wargamer2010.signshop.configuration.storage.database.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 * Assists in construction of {@link CriteriaQuery<T>}
 * @param <T> The {@link jakarta.persistence.Entity} to build this query for
 */
public class SSQueryBuilder<T> {
    public final CriteriaQuery<T> criteria;
    public final Root<T> root;

    /**
     * @param builder The criteria builder
     * @param modelClass The class to search for
     */
    public SSQueryBuilder(CriteriaBuilder builder, Class<T> modelClass) {
        // Set up the criteria builder

        // Setup model
        this.criteria = builder.createQuery(modelClass);
        this.root = criteria.from(modelClass);
    }

    /**
     * Build the query for execution
     * @param session The database session this query will be executed on
     * @return A Query
     */
    public Query<T> build(Session session) {
        return session.createQuery(criteria);
    }
}
