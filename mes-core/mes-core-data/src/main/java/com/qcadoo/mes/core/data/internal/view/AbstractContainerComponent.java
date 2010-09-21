package com.qcadoo.mes.core.data.internal.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.Component;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public abstract class AbstractContainerComponent<T> extends AbstractComponent<T> implements ContainerComponent<T> {

    private final Map<String, Component<?>> components = new LinkedHashMap<String, Component<?>>();

    public AbstractContainerComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
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
    public final ViewValue<T> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewValue<T> value = new ViewValue<T>();

        for (Component<?> component : components.values()) {
            JSONObject componentViewObject = viewObject != null ? viewObject.getJSONObject("components").getJSONObject(
                    component.getName()) : null;
            ViewValue<?> componentViewValue = component.castValue(entity, selectedEntities, componentViewObject);
            value.addComponent(component.getName(), componentViewValue);
        }

        return value;
    }

    @Override
    public final ViewValue<T> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<T> viewValue, final Set<String> pathsToUpdate) {

        ViewValue<T> value = new ViewValue<T>();

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

    @Override
    public final boolean isContainer() {
        return true;
    }

}
