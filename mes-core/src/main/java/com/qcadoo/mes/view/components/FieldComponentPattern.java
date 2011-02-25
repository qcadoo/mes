package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import com.google.common.collect.Lists;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

public abstract class FieldComponentPattern extends AbstractComponentPattern {

    private int labelWidth = 30;

    private boolean defaultRequired = false;

    public FieldComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);

    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("labelWidth".equals(option.getType())) {
                labelWidth = Integer.parseInt(option.getValue());
            } else if ("required".equals(option.getType())) {
                defaultRequired = Boolean.parseBoolean(option.getValue());
            }
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, Object> translations = new HashMap<String, Object>();

        List<String> codes = Lists.newArrayList(getTranslationPath() + ".label");

        if (getFieldDefinition() != null) {
            codes.add(getTranslationService().getEntityFieldBaseMessageCode(getFieldDefinition().getDataDefinition(),
                    getFieldDefinition().getName())
                    + ".label");

            if (BelongsToType.class.isAssignableFrom(getFieldDefinition().getType().getClass())) {
                codes.add(getTranslationService().getEntityFieldBaseMessageCode(
                        ((BelongsToType) getFieldDefinition().getType()).getDataDefinition(), getFieldDefinition().getName())
                        + ".label");
            }
        }

        translations.put("label", getTranslationService().translate(codes, locale));

        if (isHasDescription()) {
            translations.put("description", getTranslationService().translate(getTranslationPath() + ".description", locale));
            List<String> headerCodes = Lists.newArrayList(getTranslationPath() + "." + getPath() + ".descriptionHeader",
                    "core.form.descriptionHeader");
            translations.put("descriptionHeader", getTranslationService().translate(headerCodes, locale));
        }

        options.put("translations", translations);
        options.put("labelWidth", labelWidth);

        return options;
    }

    public FieldDefinition getFieldComponentFieldDefinition() {
        return getFieldDefinition();
    }

    public boolean isRequired() {
        if (getFieldDefinition() != null) {
            return getFieldDefinition().isRequired() || defaultRequired;
        } else {
            return defaultRequired;
        }
    }
}
