package com.qcadoo.mes.core.data.definition.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;

public abstract class ContainerDefinition<T> extends ComponentDefinition<T> {

    private final Map<String, ComponentDefinition<?>> components = new LinkedHashMap<String, ComponentDefinition<?>>();

    public ContainerDefinition(final String name, final ContainerDefinition<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    public Map<String, ComponentDefinition<?>> getComponents() {
        return components;
    }

    public void addComponent(final ComponentDefinition<?> component) {
        components.put(component.getName(), component);
    }

    @Override
    public ViewEntity<T> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewEntity<T> value = new ViewEntity<T>();
        for (ComponentDefinition<?> component : components.values()) {
            value.addComponent(component.getName(), component.castValue(entity, selectedEntities, viewObject != null ? viewObject
                    .getJSONObject("components").getJSONObject(component.getName()) : null));
        }
        return value;
    }

    @Override
    public ViewEntity<T> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<T> viewEntity, final Set<String> pathsToUpdate) {

        ViewEntity<T> value = new ViewEntity<T>();

        boolean isAnyNotNull = false;

        for (ComponentDefinition<?> component : components.values()) {
            ViewEntity<?> componentViewEntity = component.getValue(entity, selectedEntities,
                    viewEntity != null ? viewEntity.getComponent(component.getName()) : null, pathsToUpdate);
            if (componentViewEntity != null) {
                isAnyNotNull = true;
                value.addComponent(component.getName(), componentViewEntity);
            }
        }

        if (isAnyNotNull) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public boolean isContainer() {
        return true;
    }

}
