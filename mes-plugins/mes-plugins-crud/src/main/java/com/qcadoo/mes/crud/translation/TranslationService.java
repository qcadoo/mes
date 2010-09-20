package com.qcadoo.mes.crud.translation;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.ViewDefinition;

public interface TranslationService {

    Map<String, String> getCommonsTranslations(Locale locale);

    Map<String, String> getLoginTranslations(Locale locale);

    void updateTranslationsForViewDefinition(ViewDefinition viewDefinition, Map<String, String> translationsMap, Locale locale);

    void translateEntity(Entity entity, Locale locale);
}
