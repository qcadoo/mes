package com.qcadoo.mes.core.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.model.DataDefinition;

public interface TranslationService {

    Map<String, String> getCommonsTranslations(Locale locale);

    Map<String, String> getLoginTranslations(Locale locale);

    // void updateTranslationsForViewDefinition(ViewDefinition viewDefinition, Map<String, String> translationsMap, Locale
    // locale);
    //
    // void translateEntity(Entity entity, Locale locale);
    String translate(final String messageCode, final Locale locale);

    String translate(final String messageCode, final Object[] args, final Locale locale);

    String translate(final List<String> messageCodes, final Locale locale);

    String translate(final List<String> messageCodes, final Object[] args, final Locale locale);

    String getEntityFieldMessageCode(DataDefinition dataDefinition, String fieldName);

}
