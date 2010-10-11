package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;

public final class PasswordInputComponent extends SimpleFieldComponent {

    public PasswordInputComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "passwordInput";
    }

    @Override
    public String convertToViewValue(final String value) {
        return "";
    }

    @Override
    public String convertToDatabaseValue(final String value) {
        return value;
    }
}
