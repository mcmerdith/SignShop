package org.wargamer2010.signshop.configuration.database.clauses;

import org.wargamer2010.signshop.SignShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SQLForeignKey {
    public enum Action {
        CASCADE("CASCADE"),
        SET_NULL("SET NULL"),
        NO_ACTION("NO ACTION"),
        SET_DEFAULT("SET DEFAULT");

        private String sql;

        public String sql() {
            return sql;
        }

        Action(String sql) {
            this.sql = sql;
        }
    }
    private final Map<String, String> relatedColumns = new HashMap<>();
    private final String relatedColumn;
    private final String table;

    private String onDelete = null;
    private String onUpdate = null;

    public SQLForeignKey(String table, String relatedColumn) {
        this.table = table;
        this.relatedColumn = relatedColumn;
    }

    public SQLForeignKey onDelete(Action action) {
        onDelete = action.sql();
        return this;
    }

    public SQLForeignKey onUpdate(Action action) {
        onUpdate = action.sql();
        return this;
    }

    public SQLForeignKey relateColumns(String column, String relatedColumn) {
        return this;
    }

    public String name() {
        List<String> allColumns = new ArrayList<>(relatedColumns.keySet());
        allColumns.add(relatedColumn);

        return allColumns.stream().map((column -> column.toUpperCase().charAt(0) + column.toLowerCase().substring(1))).sorted().collect(Collectors.joining(""));
    }

    public String parseForColumn(String column) {
        if (!relatedColumns.containsKey(column)) {
            if (relatedColumn == null) {
                SignShop.debugMessage("SQL: Cannot apply `FOREIGN KEY` to column `%s`. No related column was provided!");
                return "";
            } else {
                relatedColumns.put(column, relatedColumn);
            }
        }

        List<String> finalColumns = new ArrayList<>();
        List<String> finalRelatedColumns = new ArrayList<>();

        relatedColumns.forEach((rColumn, rRelated) -> {
            if (rColumn == null || rRelated == null) {
                SignShop.debugMessage(String.format("SQL: `FOREIGN KEY` for column `%s` is invalid (%s -> %s)",
                        column,
                        (rColumn == null) ? "NULL" : rColumn,
                        (rRelated == null) ? "NULL" : rRelated));
                return;
            }

            finalColumns.add(rColumn);
            finalRelatedColumns.add(rRelated);
        });

        return String.format("(%s) REFERENCES %s(%s)%s%s",
                String.join(",", finalColumns),
                table,
                String.join(",", finalRelatedColumns),
                (onDelete == null) ? "" : " ON DELETE " + onDelete,
                (onUpdate == null) ? "" : " ON UPDATE " + onUpdate
        );
    }
}
