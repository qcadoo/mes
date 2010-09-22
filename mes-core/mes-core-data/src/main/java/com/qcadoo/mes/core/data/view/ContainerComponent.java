package com.qcadoo.mes.core.data.view;

import java.util.Map;

public interface ContainerComponent<T> extends Component<T> {

    Map<String, Component<?>> getComponents();

    void addComponent(Component<?> component);

}