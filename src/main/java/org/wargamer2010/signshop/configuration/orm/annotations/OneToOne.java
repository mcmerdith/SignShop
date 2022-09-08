package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field is a foreign key to row in another table
 * Field must be an instance of a Java {@link java.util.Collection} or an array type
 */
@Retention(RetentionPolicy.RUNTIME)

@Documented
@Target({ElementType.FIELD})
public @interface OneToOne {
    /**
     * The name of the other table
     * Default: The name of the associated objects table
     */
    String otherTableName() default "";

    /**
     * The column in the other table that will be used as the foreign ID in this table
     */
    String otherColumnName();
}
