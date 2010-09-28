package com.qcadoo.mes.core.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.view.AbstractRootComponent;
import com.qcadoo.mes.core.view.ComponentOption;

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
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("header".equals(option.getName())) {
                header = Boolean.parseBoolean(option.getValue());
            } else if ("backButton".equals(option.getName())) {
                backButton = Boolean.parseBoolean(option.getValue());
            }
        }

        addOption("backButton", backButton);
        addOption("header", header);
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
