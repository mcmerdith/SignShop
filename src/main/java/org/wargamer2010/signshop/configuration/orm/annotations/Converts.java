package org.wargamer2010.signshop.configuration.orm.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Converts {
    Convert[] value();
}
