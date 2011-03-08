package com.qcadoo.mes.localization.internal;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.localization.TranslationService;

@Service
public class DelegateTranslationService implements TranslationService {

    @Autowired
    private com.qcadoo.mes.api.TranslationService translationService;

    @Override
    public String translate(final String code, final Locale locale, final Object... args) {
        return translationService.translate(code, locale, args);
    }

}
