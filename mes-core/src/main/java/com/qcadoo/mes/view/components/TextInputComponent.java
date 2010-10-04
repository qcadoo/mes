package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;

public final class TextInputComponent extends SimpleFieldComponent {

    public TextInputComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "textInput";
    }

    @Override
    public String convertToViewValue(final String value) {
        return value;
    }

    @Override
    public String convertToDatabaseValue(final String value) {
        return value;
    }
}
