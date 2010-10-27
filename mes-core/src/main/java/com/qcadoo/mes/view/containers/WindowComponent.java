package com.qcadoo.mes.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.AbstractRootComponent;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ViewDefinition;

public final class WindowComponent extends AbstractRootComponent {

    private boolean backButton = true;

    private boolean header = true;

    private boolean fixedHeight = false;

    private boolean minWidth = true;

    public WindowComponent(final String name, final DataDefinition dataDefinition, final ViewDefinition viewDefinition,
            final TranslationService translationService) {
        super(name, dataDefinition, viewDefinition, translationService);
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
            } else if ("fixedHeight".equals(option.getType())) {
                fixedHeight = Boolean.parseBoolean(option.getValue());
            } else if ("minWidth".equals(option.getType())) {
                minWidth = Boolean.parseBoolean(option.getValue());
            }
        }

        addOption("backButton", backButton);
        addOption("fixedHeight", fixedHeight);
        addOption("header", header);
        addOption("minWidth", minWidth);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        if (header) {
            String messageCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".header";
            translationsMap.put(messageCode, getTranslationService().translate(messageCode, locale));
        }
    }

}
