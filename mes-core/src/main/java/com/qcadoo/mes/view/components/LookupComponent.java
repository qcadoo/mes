package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ContainerComponent;

public class LookupComponent extends SimpleFieldComponent {

    public LookupComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "lookupComponent";
    }

    @Override
    public String convertToViewValue(final Object value) {
        return String.valueOf(value).trim();
    }

    @Override
    public Object convertToDatabaseValue(final String value) {
        return value;
    }

}
