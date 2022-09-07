package org.wargamer2010.signshop.configuration.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Documented
@Repeatable(Conversions.class)
@Target({ElementType.FIELD})
public @interface Convert {
    Converter converter;
}
