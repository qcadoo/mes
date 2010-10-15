package com.qcadoo.mes.view.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewValue;

public abstract class SimpleFieldComponent extends AbstractComponent<SimpleValue> {

    public SimpleFieldComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    public abstract String convertToViewValue(final Object value);

    public abstract Object convertToDatabaseValue(final String value);

    @Override
    public final ViewValue<SimpleValue> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        String value = null;
        if (!viewObject.isNull("value") && !viewObject.getJSONObject("value").isNull("value")) {
            value = viewObject.getJSONObject("value").getString("value");
        }
        if (StringUtils.hasText(value)) {
            SimpleValue simpleValue = new SimpleValue(convertToDatabaseValue(value));
            if (!viewObject.isNull("value") && !viewObject.getJSONObject("value").isNull("required")) {
                simpleValue.setRequired(viewObject.getJSONObject("value").getBoolean("required"));
            }
            return new ViewValue<SimpleValue>(simpleValue);
        } else {
            return new ViewValue<SimpleValue>();
        }
    }

    @Override
    public final ViewValue<SimpleValue> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<SimpleValue> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {
        Object value = getFieldValue(entity, selectedEntities);

        SimpleValue simpleValue = null;

        if (value != null) {
            simpleValue = new SimpleValue(convertToViewValue(value));
        } else {
            simpleValue = new SimpleValue(null);
        }

        ViewValue<SimpleValue> newViewValue = new ViewValue<SimpleValue>(simpleValue);

        FieldDefinition fieldDefinition = getFieldDefinition();

        if (fieldDefinition.isRequired() || (entity == null && fieldDefinition.isRequiredOnCreate())) {
            newViewValue.getValue().setRequired(true);
        }

        if (fieldDefinition.isReadOnly() || (entity != null && fieldDefinition.isReadOnlyOnUpdate())) {
            newViewValue.setEnabled(false);
        }

        ErrorMessage validationError = getErrorMessage(entity, selectedEntities);

        if (validationError != null) {
            newViewValue.addErrorMessage(getTranslationService().translateErrorMessage(validationError, locale));
        }

        return newViewValue;
    }

    @Override
    public final void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label");
        messageCodes.add(getTranslationService().getEntityFieldMessageCode(getDataDefinition(), getName()));
        translationsMap.put(messageCodes.get(0), getTranslationService().translate(messageCodes, locale));
        if (isHasDescription()) {
            String descriptionCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".description";
            translationsMap.put(descriptionCode, getTranslationService().translate(descriptionCode, locale));
        }
    }

    private Object getFieldValue(final Entity entity, final Map<String, Entity> selectedEntities) {
        Object value = null;

        if (getSourceComponent() != null) {
            value = getFieldValue(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else if (getSourceFieldPath() != null) {
            value = getFieldValue(entity, getSourceFieldPath());
        } else {
            value = getFieldValue(entity, getFieldPath());
        }

        return value;
    }

    private ErrorMessage getErrorMessage(final Entity entity, final Map<String, Entity> selectedEntities) {

        if (getSourceComponent() != null) {
            return getFieldError(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else if (getSourceFieldPath() != null) {
            return getFieldError(entity, getSourceFieldPath());
        } else {
            return getFieldError(entity, getFieldPath());
        }
    }

}
