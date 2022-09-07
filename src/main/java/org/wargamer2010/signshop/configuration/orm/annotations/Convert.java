package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Documented
@Target({ElementType.FIELD})
public @interface Convert {
    Converter converter();
}
