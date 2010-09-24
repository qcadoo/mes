package com.qcadoo.mes.core.view.containers;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.types.HasManyType;
import com.qcadoo.mes.core.validation.ErrorMessage;
import com.qcadoo.mes.core.view.AbstractContainerComponent;
import com.qcadoo.mes.core.view.Component;
import com.qcadoo.mes.core.view.ContainerComponent;
import com.qcadoo.mes.core.view.SaveableComponent;
import com.qcadoo.mes.core.view.ViewValue;

public final class FormComponent extends AbstractContainerComponent<Long> implements SaveableComponent {

    private boolean header = true;

    public FormComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "form";
    }

    @Override
    public Long castContainerValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject) throws JSONException {
        if (viewObject != null && viewObject.has("value") && !viewObject.isNull("value")) {
            return viewObject.getLong("value");
        } else {
            return null;
        }
    }

    @Override
    public Long getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Long> viewValue, final Set<String> pathsToUpdate) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public void addContainerMessages(final Entity entity, final ViewValue<Long> viewValue) {
        if (entity != null) {
            for (ErrorMessage validationError : entity.getGlobalErrors()) {
                viewValue.addErrorMessage(validationError.getMessage()); // TODO
            }
        }
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("header", header);
    }

    @SuppressWarnings("unchecked")
    private ViewValue<Long> lookViewValue(final ViewValue<Object> viewValue) {
        ViewValue<?> lookupedViewValue = viewValue;
        String[] fields = getPath().split("\\.");

        for (String field : fields) {
            lookupedViewValue = lookupedViewValue.getComponent(field);
            if (lookupedViewValue == null) {
                return null;
            }
        }

        return (ViewValue<Long>) lookupedViewValue;

    }

    @Override
    public Entity getSaveableEntity(final ViewValue<Object> viewValue) {
        ViewValue<Long> formValue = lookViewValue(viewValue);
        Entity entity = new Entity(formValue.getValue());

        for (Map.Entry<String, Component<?>> component : getComponents().entrySet()) {
            String fieldPath = component.getValue().getFieldPath();

            if (fieldPath == null || fieldPath.split("\\.").length > 1) {
                continue;
            }

            if (getDataDefinition().getField(fieldPath).getType() instanceof HasManyType) {
                continue;
            }

            setEntityField(entity, fieldPath, component.getKey(), formValue);
        }

        return entity;
    }

    private Object setEntityField(final Entity entity, final String fieldPath, final String fieldName,
            final ViewValue<Long> formValue) {
        ViewValue<?> componentValue = formValue.getComponent(fieldName);
        Object value = componentValue.getValue();

        if (value == null) {
            entity.setField(fieldPath, null);
        } else {
            entity.setField(fieldPath, value.toString());
        }
        return value;
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

    public void setHeader(final boolean header) {
        this.header = header;
    }

}
