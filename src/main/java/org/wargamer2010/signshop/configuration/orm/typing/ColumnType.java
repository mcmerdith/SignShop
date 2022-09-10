package org.wargamer2010.signshop.configuration.orm.typing;

public class ColumnType {
    public static final SqlType.Size DEFAULT_SIZE = SqlType.Size.NONE;
    public static final int DEFAULT_LENGTH = 255;
    public static final int DEFAULT_PRECISION = 0;
    public static final int DEFAULT_SCALE = 5;

    public final SqlType type;
    public final SqlType.Size size;
    public final int length;
    public final int precision;
    public final int scale;

    public ColumnType(SqlType type, SqlType.Size size, int length, int precision, int scale) {
        this.type = type;
        this.size = size;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
    }

    public ColumnType(SqlType type, SqlType.Size size) {
        this(type, size, DEFAULT_LENGTH, DEFAULT_PRECISION, DEFAULT_SCALE);
    }

    public ColumnType(SqlType type, int length) {
        this(type, DEFAULT_SIZE, length, DEFAULT_PRECISION, DEFAULT_SCALE);
    }

    public ColumnType(SqlType type) {
        this(type, DEFAULT_SIZE, DEFAULT_LENGTH, DEFAULT_PRECISION, DEFAULT_SCALE);
    }

    public String getSql(SqlDialect dialect) {
        //TODO actually format this right
        return type.getValue(dialect == SqlDialect.SQLITE, size);
    }

    public static class Builder {
        private SqlType type;
        private SqlType.Size size;

        private int length = -1;
        private int precision = -1;
        private int scale = -1;

        public Builder setType(SqlType type) {
            this.type = type;
            return this;
        }

        public Builder setSize(SqlType.Size size) {
            this.size = size;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }

        public Builder setPrecision(int precision, int scale) {
            this.precision = precision;
            this.scale = scale;
            return this;
        }

        public ColumnType build() {
            return new ColumnType(
                    (type == null) ? SqlType.AUTO : type,
                    (size == null) ? DEFAULT_SIZE : size,
                    (length < 1) ? DEFAULT_LENGTH : length,
                    (precision < 0) ? DEFAULT_PRECISION : precision,
                    (scale < 0) ? DEFAULT_SCALE : scale
            );
        }
    }
}
