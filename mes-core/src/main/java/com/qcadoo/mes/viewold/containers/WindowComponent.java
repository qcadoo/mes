/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.viewold.AbstractRootComponent;
import com.qcadoo.mes.viewold.ComponentOption;
import com.qcadoo.mes.viewold.ViewDefinition;

/**
 * Represents window. Window has his own ribbon and 'page effect'.<br/>
 * <br/>
 * XML declaration: <br/>
 * 
 * <pre>
 *      {@code <component type="window" name="{identifier of component}">}
 * </pre>
 * 
 * XML options:
 * <ul>
 * <li>fixedHeight - [true | false] - if true, component and its content always resize with browser window</li>
 * <li>header - [true | false] - true when window contains header</li>
 * </ul>
 */
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
