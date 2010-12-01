/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.viewold.ContainerComponent;

/**
 * Represents text area element.
 */
public final class TextAreaComponent extends SimpleFieldComponent {

    private int rows = 4;

    public TextAreaComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public void initializeComponent() {
        super.initializeComponent();

        for (ComponentOption option : getRawOptions()) {
            if ("rows".equals(option.getType())) {
                rows = Integer.parseInt(option.getValue());
            }
        }
    }

    @Override
    public String getType() {
        return "textArea";
    }

    @Override
    public String convertToViewValue(final Object value) {
        return String.valueOf(value).trim();
    }

    @Override
    public Object convertToDatabaseValue(final String value) {
        return value;
    }

    public int getRows() {
        return rows;
    }

}
