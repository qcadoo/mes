package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

public abstract class FieldComponentPattern extends AbstractComponentPattern {

    public FieldComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, Object> translations = new HashMap<String, Object>();

        List<String> codes = Lists.newArrayList(getTranslationPath() + ".label", getTranslationService()
                .getEntityFieldBaseMessageCode(getDataDefinition(), getName()) + ".label");
        translations.put("label", getTranslationService().translate(codes, locale));

        if (isHasDescription()) {
            translations.put("description", getTranslationService().translate(getTranslationPath() + ".description", locale));
            List<String> headerCodes = Lists.newArrayList(getTranslationPath() + "." + getPath() + ".descriptionHeader",
                    "core.form.descriptionHeader");
            translations.put("descriptionHeader", getTranslationService().translate(headerCodes, locale));
        }

        options.put("translations", translations);

        return options;
    }
}
