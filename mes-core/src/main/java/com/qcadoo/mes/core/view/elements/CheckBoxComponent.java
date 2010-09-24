package com.qcadoo.mes.core.view.elements;

import com.qcadoo.mes.core.view.ContainerComponent;

public final class CheckBoxComponent extends SimpleFieldComponent {

    public CheckBoxComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource) {
        super(name, parent, fieldName, dataSource);
    }

    @Override
    public String getType() {
        return "checkBox";
    }

    @Override
    public String convertToViewValue(final String value) {
        if ("true".equals(value)) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public String convertToDatabaseValue(final String value) {
        return value;
    }
}
