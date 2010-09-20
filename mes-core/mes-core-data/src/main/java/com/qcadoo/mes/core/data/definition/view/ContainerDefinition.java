package com.qcadoo.mes.core.data.definition.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public ViewEntity<T> castComponentValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewEntity<T> value = new ViewEntity<T>();
        for (ComponentDefinition<?> component : components.values()) {
            value.addComponent(component.getName(), component.castValue(entity, selectedEntities, viewObject != null ? viewObject
                    .getJSONObject("components").getJSONObject(component.getName()) : null));
        }
        value.setValue(castContainerValue(entity, viewObject));
        return value;
    }

    @Override
    public ViewEntity<T> getComponentValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<T> viewEntity) {
        ViewEntity<T> value = new ViewEntity<T>();

        for (ComponentDefinition<?> component : components.values()) {
            value.addComponent(
                    component.getName(),
                    component.getValue(entity, selectedEntities, globalViewEntity,
                            viewEntity != null ? viewEntity.getComponent(component.getName()) : null));
        }

        value.setValue(getContainerValue(entity, selectedEntities, globalViewEntity, viewEntity));

        return value;
    }

    public abstract T castContainerValue(final Entity entity, final Object viewObject);

    public abstract T getContainerValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<T> viewEntity);

    @Override
    public boolean isContainer() {
        return true;
    }

}
