package com.qcadoo.mes.core.data.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.view.AbstractRootComponent;

public class WindowComponent extends AbstractRootComponent {

    private boolean backButton = true;

    private boolean header = true;

    public WindowComponent(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("backButton", backButton);
        viewOptions.put("header", header);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        if (header) {
            String messageCode = getPath() + ".header";
            translationsMap.put(messageCode, translationService.translate(messageCode, locale));
        }
    }

    public boolean isBackButton() {
        return backButton;
    }

    public void setBackButton(boolean backButton) {
        this.backButton = backButton;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }
}
