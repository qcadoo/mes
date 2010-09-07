package com.qcadoo.mes.plugins.products.mock;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.plugins.products.translation.TranslationService;

public class TranslationServiceMock implements TranslationService {

    public Map<String, String> getCommonsTranslations(Locale locale) {
        return null;
    }

    public void updateTranslationsForViewDefinition(ViewDefinition viewDefinition, Map<String, String> translationsMap,
            Locale locale) {

    }

}
