package com.qcadoo.mes.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.model.DataDefinition;

public interface TranslationService {

    Map<String, String> getCommonsMessages(Locale locale);

    Map<String, String> getSecurityMessages(Locale locale);

    String translate(final String messageCode, final Locale locale, final Object... args);

    String translate(final List<String> messageCodes, final Locale locale, final Object... args);

    String getEntityFieldMessageCode(DataDefinition dataDefinition, String fieldName);

}
