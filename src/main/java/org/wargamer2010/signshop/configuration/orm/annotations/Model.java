package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Documented
@Target({ElementType.TYPE})
public @interface Model {
    /**
     * The name of the table
     * Default: The name of the class, lowercase
     */
    String tableName() default "";
}
