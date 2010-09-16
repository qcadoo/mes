package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class ContainerDefinition extends ComponentDefinition {

    private final Map<String, ComponentDefinition> components = new LinkedHashMap<String, ComponentDefinition>();

    private String header;

    public ContainerDefinition(final String name, final String dataSource) {
        super(name, dataSource);
    }

    public Map<String, ComponentDefinition> getComponents() {
        return components;
    }

    public ComponentDefinition getComponent(String componentName) {
        return components.get(componentName);
    }

    public void addComponent(ComponentDefinition component) {
        components.put(component.getName(), component);
        // if (component.getDataSource() != null && component.getDataSource().charAt(0) == '#') {
        // String linkPath = component.getDataSource();
        // String element = linkPath.substring(linkPath.indexOf('{') + 1, linkPath.indexOf('}'));
        // String fieldPath = linkPath.substring(linkPath.indexOf('}') + 2);
        // components.get(element).
        // }
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public Map<String, Object> getValue(DataDefinition dataDefinition, DataAccessService dataAccessService, Entity entity) {
        Map<String, Object> result = new HashMap<String, Object>();
        // if (getDataSource() == null || entity == null) {
        for (ComponentDefinition component : components.values()) {
            Object value = component.getValue(dataDefinition, dataAccessService, entity);
            if (value != null) {
                result.put(component.getName(), value);
            }
        }
        // } else {
        // for (ComponentDefinition component : components.values()) {
        //
        // result.put(component.getName(), component.getValue(dataDefinition, dataAccessService, entity));
        // }
        // }
        return result;
    }

    @Override
    public Object getUpdateValues(Map<String, String> updateComponents) {
        return null;
    }
}
