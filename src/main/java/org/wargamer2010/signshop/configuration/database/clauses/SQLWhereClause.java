package org.wargamer2010.signshop.configuration.database.clauses;

import java.util.*;
import java.util.stream.Collectors;

public class SQLWhereClause implements SQLClause {
    private static class WhereComponent {
        public String key;
        public Object value;

        public boolean invert;
        public SQLComparisonType comparison;
        private final boolean nullCheck;

        public WhereComponent(String key, boolean invert, SQLComparisonType comparison, Object value) {
            this.key = key;

            this.invert = invert;
            this.comparison = comparison;
            nullCheck = comparison == SQLComparisonType.IS_NULL || comparison == SQLComparisonType.IS_NOT_NULL;

            this.value = nullCheck ? null : value;
        }

        public String parse() {
            return parse(true);
        }

        public String parse(boolean parameterize) {
            String specialChecks = parameterize ? " ?" : "";

            if (nullCheck) {
                specialChecks = "";
            } else if (comparison == SQLComparisonType.IN) {
                if (value instanceof Collection<?>) {
                    Collection<?> multipleValues = (Collection<?>) value;
                    specialChecks = " " + String.join(",", Collections.nCopies(multipleValues.size(), "?"));
                }
            } else if (comparison == SQLComparisonType.BETWEEN) {
                if (value instanceof Collection<?>) {
                    ArrayList<?> multipleValues = new ArrayList<>((Collection<?>) value);
                    if (multipleValues.size() >= 2) {
                        specialChecks = String.format(" %s AND %s", multipleValues.get(0), multipleValues.get(1));
                    }
                }
            }

            return String.format("%s %s%s%s", key, invert ? "NOT " : "", comparison.sql(), specialChecks);
        }
    }

    private final List<WhereComponent> whereComponents = new ArrayList<>();
    private SQLComparisonLogic logic = SQLComparisonLogic.AND;
    private String complexClause = null;

    public SQLWhereClause addWhereComponent(String column, boolean invert, SQLComparisonType comparison, Object value) {
        whereComponents.add(new WhereComponent(column, invert, comparison, value));
        return this;
    }

    public SQLWhereClause addWhereComponents(Map<String, Object> columnValue, boolean invert, SQLComparisonType comparison) {
        columnValue.forEach((column, value) -> whereComponents.add(new WhereComponent(column, invert, comparison, value)));
        return this;
    }

    public SQLWhereClause setLogic(SQLComparisonLogic logic) {
        if (logic != null) this.logic = logic;
        return this;
    }

    /**
     * Set a custom clause
     * @param query The SQL query (without 'WHERE')
     */
    public SQLWhereClause makeComplexQuery(String query) {
        this.complexClause = query.trim();
        return this;
    }

    @Override
    public String toSQL() {
        return "WHERE " + ((this.complexClause != null) ? complexClause : rawComponents(true));
    }

    /**
     * Return the query without the 'WHERE'
     * @param parameterize If values should be replaced with '?'
     * @return A formatted clause without 'WHERE' for use in a CHECK constraint
     */
    public String rawComponents(boolean parameterize) {
        return whereComponents.stream().map(component -> component.parse(true)).collect(Collectors.joining(String.format(" %s ", logic.toString())));
    }

    /**
     * Return the name of the clause as a whole
     * @return An alphabetized String of the columns being checked
     */
    public String name() {
        return whereComponents.stream().map((whereComponent -> whereComponent.key.toUpperCase().charAt(0) + whereComponent.key.toLowerCase().substring(1))).sorted().collect(Collectors.joining(""));
    }

    @Override
    public List<Object> values() {
        List<Object> values = new ArrayList<>();

        whereComponents.forEach((component) -> {
            if (component.value instanceof Collection<?>) {
                Collection<?> multipleValue = (Collection<?>) component.value;
                values.addAll(multipleValue);
            } else {
                values.add(component.value);
            }
        });

        return values;
    }
}