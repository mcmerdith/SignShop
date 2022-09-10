package org.wargamer2010.signshop.configuration.orm.annotations;

public @interface Default {
    /**
     * The default value formatted as a String
     * This value is inserted directly into the SQL statement without escaping, so be cautious
     */
    String value();
}
