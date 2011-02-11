package com.qcadoo.mes.view;

public interface ViewDefinitionState extends ContainerState {

    void performEvent(String path, String event, String... args);

    ComponentState getComponentByReference(String reference);

    void redirectTo(String redirectToUrl, boolean openInNewWindow, boolean shouldSerialize);

    void registerComponent(String reference, String path, ComponentState state);

}
