package com.qcadoo.mes.core.data.view;

import java.util.Map;

public interface ContainerComponent<T> extends ComponentDefinition<T> {

    Map<String, ComponentDefinition<?>> getComponents();

    void addComponent(ComponentDefinition<?> component);

}