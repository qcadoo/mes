package com.qcadoo.mes.core.data.view.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public abstract class SimpleFieldComponent extends AbstractComponent<String> {

    public SimpleFieldComponent(final String name, final ContainerComponent parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    public abstract String convertToViewValue(final String value);

    public abstract String convertToDatabaseValue(final String value);

    @Override
    public final ViewValue<String> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        String value = viewObject.getString("value");
        if (StringUtils.hasText(value)) {
            return new ViewValue<String>(convertToDatabaseValue(value.trim()));
        } else {
            return null;
        }
    }

    @Override
    public final ViewValue<String> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<String> viewEntity, final Set<String> pathsToUpdate) {
        String value = getStringValue(entity, selectedEntities);

        if (StringUtils.hasText(value)) {
            return new ViewValue<String>(convertToViewValue(value.trim()));
        } else {
            return null;
        }
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewName() + "." + getPath() + ".label");
        messageCodes.add(getDataDefinition().getName() + "." + getName() + ".label");
        translationsMap.put(getPath() + ".label", translationService.translate(messageCodes, locale));
    }

    private String getStringValue(final Entity entity, final Map<String, Entity> selectedEntities) {
        Object value = null;

        if (getSourceComponent() != null) {
            value = getFieldValue(selectedEntities.get(getSourceComponent().getPath()), getSourceFieldPath());
        } else {
            value = getFieldValue(entity, getFieldPath());
        }

        if (value == null) {
            return "";
        } else {
            return String.valueOf(value);
        }
    }

}