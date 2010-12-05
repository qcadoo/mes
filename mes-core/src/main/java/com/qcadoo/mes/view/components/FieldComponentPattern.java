package com.qcadoo.mes.view.components;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("input")
public abstract class FieldComponentPattern extends AbstractComponentPattern {

    public FieldComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected final JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject translations = new JSONObject();

        List<String> codes = Lists.newArrayList(getTranslationPath() + ".label", getTranslationService()
                .getEntityFieldBaseMessageCode(getDataDefinition(), getName()) + ".label");
        translations.put("label", getTranslationService().translate(codes, locale));

        if (isHasDescription()) {
            translations.put("description", getTranslationService().translate(getTranslationPath() + ".description", locale));
            List<String> headerCodes = Lists.newArrayList(getTranslationPath() + "." + getPath() + ".descriptionHeader",
                    "core.form.descriptionHeader");
            translations.put("descriptionHeader", getTranslationService().translate(headerCodes, locale));
        }

        json.put("translations", translations);

        return json;
    }
}
