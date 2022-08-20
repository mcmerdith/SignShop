package org.wargamer2010.signshop.configuration.database.clauses;

import java.util.List;

public interface SQLClause {
    String toSQL();

    List<Object> values();
}
