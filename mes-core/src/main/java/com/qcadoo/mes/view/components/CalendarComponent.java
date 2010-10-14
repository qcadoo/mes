package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;

public final class CalendarComponent extends SimpleFieldComponent {

    public CalendarComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "calendar";
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
