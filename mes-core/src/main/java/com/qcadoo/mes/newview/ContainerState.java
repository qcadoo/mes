package com.qcadoo.mes.newview;

import java.util.Map;

public interface ContainerState extends ComponentState {

    Map<String, ComponentState> getChildren();

    ComponentState getChild(String name);

    void addChild(ComponentState state);

}
