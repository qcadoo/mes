package com.qcadoo.mes.newview;

public interface ViewDefinitionState extends ContainerState {

    ComponentState lookupComponent(String path);

    void performEvent(String path, String event, String... args);

}
