package org.wargamer2010.signshop.configuration.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)

@Documented
@Target({ElementType.TYPE})
public @interface Table {
    String name();
}
