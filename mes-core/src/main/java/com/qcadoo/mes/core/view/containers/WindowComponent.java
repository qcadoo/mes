package com.qcadoo.mes.core.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.view.AbstractRootComponent;

public final class WindowComponent extends AbstractRootComponent {

    private boolean backButton = true;

    private boolean header = true;

    public WindowComponent(final String name, final DataDefinition dataDefinition, final String viewName) {
        super(name, dataDefinition, viewName);
    }

    @Override
    public String getType() {
        return "window";
    }

    @Override
    public void getComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("backButton", backButton);
        viewOptions.put("header", header);
    }

    @Override
    public void addComponentOption(final String name, final String value) {
        if ("header".equals(name)) {
            header = Boolean.parseBoolean(value);
        } else if ("backButton".equals(name)) {
            backButton = Boolean.parseBoolean(value);
        }
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        if (header) {
            String messageCode = getViewName() + "." + getPath() + ".header";
            translationsMap.put(messageCode, translationService.translate(messageCode, locale));
        }
    }

    public boolean isBackButton() {
        return backButton;
    }

    public boolean isHeader() {
        return header;
    }

}
