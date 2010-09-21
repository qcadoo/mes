package com.qcadoo.mes.core.data.view;

import java.util.Map;

public interface ContainerComponent<T> extends ComponentDefinition<T> {

    public abstract Map<String, ComponentDefinition<?>> getComponents();

    public abstract void addComponent(final ComponentDefinition<?> component);

}