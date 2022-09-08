package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this column is the ID for the row
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /**
     * If the key should auto-increment
     * Only valid for INTEGER columns
     */
    boolean autoIncrement() default false;
}
