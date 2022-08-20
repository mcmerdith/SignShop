package org.wargamer2010.signshop.configuration.database.clauses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLOrderByClause implements SQLClause {
    public enum SQLSortOrder {
        ASC,
        DESC
    }

    private Map<String, SQLSortOrder> sorting = new HashMap<>();

    public SQLOrderByClause(Map<String, SQLSortOrder> sorting) {
        if (sorting != null) this.sorting.putAll(sorting);
    }

    @Override
    public String toSQL() {
        List<String> sorts = new ArrayList<>();
        sorting.forEach((column, order) -> sorts.add(column + order.toString()));
        return "ORDER BY " + String.join(",", sorts);
    }

    @Override
    public List<Object> values() {
        return new ArrayList<>();
    }
}
