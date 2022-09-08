package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Mark this field for exclusion from the database
 */
@Retention(RetentionPolicy.RUNTIME)

@Documented
@Target({ElementType.FIELD})
public @interface Transient {
}
