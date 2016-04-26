package com.qcadoo.mes.basic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SearchAttribute {

    public static enum SEARCH_TYPE {
        EXACT_MATCH, LIKE
    }

    SEARCH_TYPE searchType() default SEARCH_TYPE.LIKE;
}
