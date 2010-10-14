package com.qcadoo.mes.view.components;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;

public final class TextAreaComponent extends SimpleFieldComponent {

    private int rows = 2;

    public TextAreaComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public void initializeComponent() {
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
    public String convertToViewValue(final String value) {
        return value;
    }

    @Override
    public String convertToDatabaseValue(final String value) {
        return value;
    }

    public int getRows() {
        return rows;
    }

}
