package com.qcadoo.model.api.localization;

import java.util.Locale;

public interface TranslationService {

    String translate(String code, Locale locale, Object... args);

}
