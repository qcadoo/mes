package com.qcadoo.mes.newview;

public interface ViewDefinitionState extends ContainerState {

    void performEvent(String path, String event, String... args);

    ComponentState getComponentByPath(String path);

}
