package org.wargamer2010.signshop.configuration.orm.annotations;

import org.wargamer2010.signshop.configuration.orm.typing.SqlType;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * @return
     */
    String name() default "";

    SqlType definition() default SqlType.AUTO;

    SqlType.Size size() default SqlType.Size.NONE;

    boolean nullable() default true;

    boolean unique() default false;

    String customDefinition() default "";

    int length() default 255;

    int precision() default 0;

    int scale() default 5;

    String check() default "";
}


