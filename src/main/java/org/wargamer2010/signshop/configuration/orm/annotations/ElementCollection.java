package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field should be stored in a separate associated table
 * Field must be an instance of {@link java.util.Collection} or an array type
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementCollection {
    /**
     * The name of the table the elements will be stored in
     * Default: (parent table)_(column name)s
     */
    String associatedTableName() default "";

    /**
     * The name of the column in the associated secondary table
     * Default: (parent table)_id
     */
    String referenceColumnName() default "";

    /**
     * The name of the column that each value will be stored in
     * Default: (column name)
     */
    String valueColumnName() default "";
}
