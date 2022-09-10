package org.wargamer2010.signshop.configuration.orm.annotations;

import org.wargamer2010.signshop.configuration.orm.typing.conversion.SSAttributeConverter;

import java.lang.annotation.*;

/**
 * Apply a type converter to this field
 * Type converters must return a type that can be natively stored in the database
 */
@Documented
@Repeatable(Converts.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Convert {
    /**
     * The converter that should be used
     */
    Class<? extends SSAttributeConverter<?, ?>> converter();
}
