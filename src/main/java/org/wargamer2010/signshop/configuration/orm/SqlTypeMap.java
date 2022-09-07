package org.wargamer2010.signshop.configuration.orm;

import java.sql.SQLType;

public enum SqlTypeMap {
    STRING;

    SqlTypeMap(Class<?> javaClass, SQLType type) {

    }
}
