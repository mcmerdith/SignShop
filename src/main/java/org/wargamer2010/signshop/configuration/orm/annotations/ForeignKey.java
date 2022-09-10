package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

/**
 * Specify that this field should be stored in a separate associated table
 * Field type must be a {@link Model}
 */
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    /**
     * The name of the table the element is stored in
     * Default: The table name of the model represented by the annotated field's class
     */
    String associatedTableName() default "";

    /**
     * The column in the other table that will be used as the foreign ID
     * Default: the primary key of the other table
     */
    String associatedColumnName() default "";
}
