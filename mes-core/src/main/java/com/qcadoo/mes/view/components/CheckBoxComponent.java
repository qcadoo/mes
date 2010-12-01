/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;

/**
 * Represents checkbox element.
 */
public final class CheckBoxComponent extends SimpleFieldComponent {

    private boolean textRepresentationOnDisabled = false;

    public CheckBoxComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "checkBox";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("textRepresentationOnDisabled".equals(option.getType())) {
                textRepresentationOnDisabled = Boolean.parseBoolean(option.getValue());
            }
        }

        addOption("textRepresentationOnDisabled", textRepresentationOnDisabled);
    }

    @Override
    public String convertToViewValue(final Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        }
        if ("true".equals(String.valueOf(value).trim())) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public Object convertToDatabaseValue(final String value) {
        return value;
    }
}
