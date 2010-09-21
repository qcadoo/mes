package com.qcadoo.mes.core.data.view;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;

public abstract class AbstractContainerComponent extends AbstractComponent<Object> implements ContainerComponent {

    private final Map<String, Component<?>> components = new LinkedHashMap<String, Component<?>>();

    public AbstractContainerComponent(final String name, final ContainerComponent parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public final Map<String, Component<?>> getComponents() {
        return components;
    }

    @Override
    public final void addComponent(final Component<?> component) {
        components.put(component.getName(), component);
    }

    @Override
    public final ViewValue<Object> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewValue<Object> value = new ViewValue<Object>();

        for (Component<?> component : components.values()) {
            JSONObject componentViewObject = viewObject != null ? viewObject.getJSONObject("components").getJSONObject(
                    component.getName()) : null;
            ViewValue<?> componentViewValue = component.castValue(entity, selectedEntities, componentViewObject);
            value.addComponent(component.getName(), componentViewValue);
        }

        return value;
    }

    @Override
    public final ViewValue<Object> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> viewValue, final Set<String> pathsToUpdate) {

        ViewValue<Object> value = new ViewValue<Object>();

        boolean isAnyViewValueNotNull = false;

        for (Component<?> component : components.values()) {
            ViewValue<?> componentViewValue = viewValue != null ? viewValue.getComponent(component.getName()) : null;
            ViewValue<?> newViewValue = component.getValue(entity, selectedEntities, componentViewValue, pathsToUpdate);
            if (newViewValue != null) {
                isAnyViewValueNotNull = true;
                value.addComponent(component.getName(), newViewValue);
            }
        }

        if (isAnyViewValueNotNull) {
            return value;
        } else {
            return null;
        }
    }

    public final void updateComponentsTranslations(Map<String, String> translationsMap,
            final TranslationService translationService, final Locale locale) {
        for (Component<?> component : components.values()) {
            component.updateTranslations(translationsMap, translationService, locale);
        }
    }

    @Override
    public final boolean isContainer() {
        return true;
    }

}
