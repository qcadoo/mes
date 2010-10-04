package com.qcadoo.mes.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.model.DataDefinition;

public interface TranslationService {

    Map<String, String> getCommonsTranslations(Locale locale);

    Map<String, String> getLoginTranslations(Locale locale);

    String translate(final String messageCode, final Locale locale, final Object... args);

    String translate(final List<String> messageCodes, final Locale locale, final Object... args);

    String getEntityFieldMessageCode(DataDefinition dataDefinition, String fieldName);

}
