package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;

public final class CheckBoxComponent extends SimpleFieldComponent {

    public CheckBoxComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "checkBox";
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
