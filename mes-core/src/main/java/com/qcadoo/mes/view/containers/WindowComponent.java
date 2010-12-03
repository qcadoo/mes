/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.AbstractRootComponent;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ViewDefinition;

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
