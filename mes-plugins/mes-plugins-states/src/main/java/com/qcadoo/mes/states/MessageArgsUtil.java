package com.qcadoo.mes.states;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public final class MessageArgsUtil {

    public static final String ARGS_SEPARATOR = "@#@";

    private MessageArgsUtil() {
    }

    public static String join(final String[] splittedArgs) {
        if (ArrayUtils.isEmpty(splittedArgs)) {
            return null;
        }
        return StringUtils.join(splittedArgs, ARGS_SEPARATOR);
    }

    public static String[] split(final String joinedArgs) {
        if (StringUtils.isBlank(joinedArgs)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return joinedArgs.split(ARGS_SEPARATOR);
    }

}
