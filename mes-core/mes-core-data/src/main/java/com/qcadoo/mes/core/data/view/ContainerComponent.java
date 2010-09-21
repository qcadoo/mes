package com.qcadoo.mes.core.data.view;

import java.util.Map;

public interface ContainerComponent extends Component<Object> {

    Map<String, Component<?>> getComponents();

    void addComponent(Component<?> component);

}