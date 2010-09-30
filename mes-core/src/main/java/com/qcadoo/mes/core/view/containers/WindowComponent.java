package com.qcadoo.mes.core.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.view.AbstractRootComponent;
import com.qcadoo.mes.core.view.ComponentOption;
import com.qcadoo.mes.core.view.ViewDefinition;
import com.qcadoo.mes.model.DataDefinition;

public final class WindowComponent extends AbstractRootComponent {

    private boolean backButton = true;

    private boolean header = true;

    public WindowComponent(final String name, final DataDefinition dataDefinition, final ViewDefinition viewDefinition) {
        super(name, dataDefinition, viewDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else if ("backButton".equals(option.getType())) {
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
            String messageCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".header";
            translationsMap.put(messageCode, translationService.translate(messageCode, locale));
        }
    }

}
