package com.qcadoo.mes.plugins.products.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

@Controller
public class TranslationService {

    @Autowired
    private MessageSource messageSource;

    public String translate(String messageCode, Locale locale) {
        return messageSource.getMessage(messageCode, null, "TO TRANSLATE: " + messageCode, locale);
    }

    public String translate(String messageCode, Object[] args, Locale locale) {
        return messageSource.getMessage(messageCode, args, "TO TRANSLATE: " + messageCode, locale);
    }
}
