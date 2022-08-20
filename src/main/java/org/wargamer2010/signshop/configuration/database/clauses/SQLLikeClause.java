package org.wargamer2010.signshop.configuration.database.clauses;

import java.util.Collections;
import java.util.List;

public class SQLLikeClause implements SQLClause {
    private String column;
    private String pattern;
    private boolean invert;

    public SQLLikeClause(String column, String pattern) {
        this.column = column;
        this.pattern = pattern;
        this.invert = false;
    }

    public SQLLikeClause(String column, String pattern, boolean invert) {
        this.column = column;
        this.pattern = pattern;
        this.invert = invert;
    }

    @Override
    public String toSQL() {
        return String.format("WHERE %s %sLIKE '?'", column, (invert) ? "NOT " : "");
    }

    @Override
    public List<Object> values() {
        return Collections.singletonList(pattern);
    }
}
