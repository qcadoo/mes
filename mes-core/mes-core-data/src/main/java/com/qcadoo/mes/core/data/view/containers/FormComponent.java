package com.qcadoo.mes.core.data.view.containers;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.view.AbstractContainerComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public final class FormComponent extends AbstractContainerComponent {

    private boolean header = true;

    public FormComponent(final String name, final ContainerComponent parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "form";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("header", header);
    }

    public Entity getFormEntity(final ViewValue<Object> viewEntity, final String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object addValidationResults(final ViewValue<Object> viewEntity, final String path, final Entity results) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        if (header) {
            String messageCode = getViewName() + "." + getPath() + ".header";
            translationsMap.put(messageCode, translationService.translate(messageCode, locale));
        }
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

}
