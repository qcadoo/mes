package com.qcadoo.mes.view;

import java.util.Map;

public interface ContainerPattern extends ComponentPattern {

    Map<String, ComponentPattern> getChildren();

    ComponentPattern getChild(String name);

    void addChild(ComponentPattern componentPattern);

}
