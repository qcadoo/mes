package com.qcadoo.mes.plugins.products.mock;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

public class MessageSourceMock implements MessageSource {

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        return null;
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        return "TR" + code;
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return "TR" + code;
    }

}
