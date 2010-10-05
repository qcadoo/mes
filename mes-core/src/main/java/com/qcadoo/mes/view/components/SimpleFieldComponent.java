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
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewValue;

public abstract class SimpleFieldComponent extends AbstractComponent<String> {

    public SimpleFieldComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    public abstract String convertToViewValue(final String value);

    public abstract String convertToDatabaseValue(final String value);

    @Override
    public final ViewValue<String> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        String value = null;
        if (!viewObject.isNull("value")) {
            value = viewObject.getString("value");
        }
        if (StringUtils.hasText(value)) {
            return new ViewValue<String>(convertToDatabaseValue(value.trim()));
        } else {
            return new ViewValue<String>();
        }
    }

    @Override
    public final ViewValue<String> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<String> viewValue, final Set<String> pathsToUpdate) {
        String value = getStringValue(entity, selectedEntities);

        ViewValue<String> newViewValue = new ViewValue<String>(convertToViewValue(value.trim()));

        String errorMessage = getErrorMessage(entity, selectedEntities);

        if (errorMessage != null) {
            newViewValue.addErrorMessage(errorMessage);
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
    }

    private String getStringValue(final Entity entity, final Map<String, Entity> selectedEntities) {
        Object value = null;

        if (getSourceComponent() != null) {
            value = getFieldValue(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else if (getSourceFieldPath() != null) {
            value = getFieldValue(entity, getSourceFieldPath());
        } else {
            value = getFieldValue(entity, getFieldPath());
        }

        if (value == null) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

    private String getErrorMessage(final Entity entity, final Map<String, Entity> selectedEntities) {
        ErrorMessage value = null;

        if (getSourceComponent() != null) {
            value = getFieldError(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else if (getSourceFieldPath() != null) {
            value = getFieldError(entity, getSourceFieldPath());
        } else {
            value = getFieldError(entity, getFieldPath());
        }

        if (value == null) {
            return null;
        } else {
            return value.getMessage(); // TODO masz i18n
        }
    }

}
