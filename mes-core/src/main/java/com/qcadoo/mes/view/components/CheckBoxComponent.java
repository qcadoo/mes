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
