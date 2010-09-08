package com.qcadoo.mes.plugins.products.translation;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.data.definition.view.ViewDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public interface TranslationService {

    Map<String, String> getCommonsTranslations(Locale locale);

    Map<String, String> getLoginTranslations(Locale locale);

    void updateTranslationsForViewDefinition(ViewDefinition viewDefinition, Map<String, String> translationsMap, Locale locale);

    void translateValidationResults(ValidationResults validationResults, Locale locale);
}
