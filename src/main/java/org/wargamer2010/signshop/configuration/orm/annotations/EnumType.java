package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field is a foreign key to row in another table
 * Field must be an instance of a Java {@link java.util.Collection} or an array type
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumType {
    enum Mode {
        ORDINAL,
        VALUE
    }

    Mode mode() default Mode.VALUE;
}
