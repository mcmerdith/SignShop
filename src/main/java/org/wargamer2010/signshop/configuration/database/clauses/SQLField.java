package org.wargamer2010.signshop.configuration.database.clauses;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.database.SQLHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLField implements SQLClause {
    private static class SQLConstraint {
        private static final String[] standardModes = {"", "ADD ", "DROP "};
        private static final String[] specialModes = {"", "SET ", "DROP "};
        private final String column;
        private final SQLConstraintType type;
        private final Object value;
        private final int mode;

        public SQLConstraint(String column, SQLConstraintType type, int mode) {
            this(column, type, null, mode);
        }

        /**
         * A SQL column
         *
         * @param column The column name
         * @param type   The constraint type
         * @param value  The constraint value
         * @param mode   The mode this field is used for in the command: 0=CREATE,1=ALTER,2=DROP
         */
        public SQLConstraint(String column, SQLConstraintType type, Object value, int mode) {
            this.column = column;
            this.type = type;
            this.value = value;
            this.mode = mode;
        }

        /**
         * Get a parsed SQL string
         *
         * @return A SQL string
         */
        @Override
        public String toString() {
            final String sql = type.sql();

            String prepared = "";

            switch (type) {
                // Inline constraints
                case AUTO_INCREMENT:
                    prepared = (mode == 2) ? "" : String.format("%s%s", sql, (value == null) ? "" : "=" + SQLHelper.prepareValue(value));
                    break;
                case NOT_NULL:
                    prepared = (mode == 2) ? "" : sql;
                    break;
                case DEFAULT:
                    prepared = String.format("%s%s%s", specialModes[mode], sql, (mode != 2) ? " " + SQLHelper.prepareValue(value) : "");
                    break;

                // External Constraints
                case FOREIGN_KEY:
                    if (value instanceof SQLForeignKey) {
                        SQLForeignKey foreignKey = (SQLForeignKey) this.value;

                        // Named foreign key
                        prepared = String.format("%sCONSTRAINT FK_%s%s",
                                standardModes[mode],
                                column,
                                (mode != 2) ? String.format(" %s %s", sql, foreignKey.parseForColumn(column)) : ""
                        );
                    }
                    break;
                case UNIQUE:
                    // UNIQUE and PK are the same so fall through
                case PRIMARY_KEY:
                    String primaryKey = ",";

                    if (value instanceof String) {
                        primaryKey += (String) this.value;
                    } else {
                        primaryKey = null;
                    }

                    if (mode == 2) {
                        prepared = standardModes[mode] + sql;
                    } else {
                        prepared = String.format("%sCONSTRAINT %sK_%s %s (%s%s)",
                                standardModes[mode],
                                type.name().charAt(0),
                                column,
                                sql,
                                column,
                                (primaryKey != null) ? "," + primaryKey : "");
                    }
                    break;
                case CHECK:
                    if (value instanceof SQLWhereClause) {
                        SQLWhereClause clause = (SQLWhereClause) value;

                        if (mode == 2) {
                            prepared = standardModes[mode] + sql + " CHK_" + clause.name();
                        } else {
                            prepared = String.format("%sCONSTRAINT CHK_%s %s (%s)",
                                    standardModes[mode],
                                    clause.name(),
                                    sql,
                                    clause.rawComponents(false)
                            );
                        }
                    }
                    break;
            }

            if (prepared.equals("")) {
                SignShop.log(
                        String.format("Invalid %s constraint (was an acceptable value passed in?) TYPEOF value: %s",
                                sql,
                                (value == null) ? "null" : value.getClass().getSimpleName()),
                        Level.WARNING
                );
            }

            return prepared;
        }
    }

    private final String column;
    private final String dataType;

    private int command;

    private final List<SQLConstraint> inlineConstraints = new ArrayList<>();
    private final List<SQLConstraint> externalConstraints = new ArrayList<>();

    /**
     * A SQL column
     * <br>For ALTER/DROP statements, use {@link SQLField#command(int)}
     *
     * @param column
     * @param dataType
     */
    public SQLField(String column, String dataType) {
        this.column = column;
        this.dataType = dataType;

        this.command(0);
    }

    /**
     * Constrain the field
     * <br>Chainable
     *
     * @param type The type of constraint
     */
    public SQLField constrain(SQLConstraintType type) {
        return constrain(type, null);
    }

    /**
     * Constrain the field
     * <br>Chainable
     *
     * @param type  The type of constraint
     * @param value (option) Value. See {@link SQLConstraintType} for acceptable values
     */
    public SQLField constrain(SQLConstraintType type, Object value) {
        if (type == null) return this;

        List<SQLConstraint> constraints = (type.isInline()) ? inlineConstraints : externalConstraints;
        constraints.add(new SQLConstraint(column, type, value, command));

        return this;
    }

    /**
     * Set the command this field is used in
     *
     * @param command 0=CREATE,1=ALTER,2=DROP
     */
    public SQLField command(int command) {
        this.command = (0 <= command && command <= 2) ? command : 0;
        return this;
    }

    public String getColumn() {
        return column;
    }

    public String getDataType() {
        return dataType;
    }

    @Override
    public String toSQL() {
        inlineConstraints.sort((c1, c2) -> (c1.value == null && c2.value != null) ? 1 : ((c1.value != null && c2.value == null) ? -1 : 0));

        return String.format("%s %s %s", column, dataType, inlineConstraints.stream().map(SQLConstraint::toString).collect(Collectors.joining(" ")));
    }

    public List<String> constraints() {
        return externalConstraints.stream().map(SQLConstraint::toString).collect(Collectors.toList());
    }

    @Override
    public List<Object> values() {
        List<Object> allConstraintValues = new ArrayList<>();

        allConstraintValues.addAll(parameterizedConstraintValues(inlineConstraints));
        allConstraintValues.addAll(parameterizedConstraintValues(externalConstraints));

        return allConstraintValues;
    }

    private List<Object> parameterizedConstraintValues(List<SQLConstraint> constraints) {
        List<Object> results = new ArrayList<>();

        constraints.forEach((constraint) -> {
            if (constraint.value != null) {
                if (constraint.type == SQLConstraintType.CHECK && constraint.value instanceof SQLWhereClause) {
                    results.addAll(((SQLWhereClause) constraint.value).values());
                } else if (constraint.type == SQLConstraintType.DEFAULT) {
                    results.add(constraint.value);
                }
            }
        });

        return results;
    }
}
