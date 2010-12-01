/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.viewold.ComponentOption;
import com.qcadoo.mes.viewold.ContainerComponent;

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
