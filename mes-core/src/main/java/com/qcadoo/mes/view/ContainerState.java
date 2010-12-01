package com.qcadoo.mes.view;

import java.util.Map;

public interface ContainerState extends ComponentState {

    Map<String, ComponentState> getChildren();

    ComponentState getChild(String name);

    void addChild(ComponentState state);

}
