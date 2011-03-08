package com.qcadoo.model.integration;

import java.util.Locale;

import com.qcadoo.model.api.localization.TranslationService;

public class MockTranslationService implements TranslationService {

    @Override
    public String translate(final String code, final Locale locale, final Object... args) {
        return code;
    }

}
