package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field should be stored in a separate associated table
 * Field must be an instance of a Java {@link java.util.Collection} or an array type
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {
    /**
     * The name of the table the elements will be stored in
     * Default: (parent table)_(column name)s
     */
    String otherTableName() default "";

    /**
     * The name of the column in the associated secondary table
     * Default: (column name)
     */
    String otherColumnName() default "";
}
