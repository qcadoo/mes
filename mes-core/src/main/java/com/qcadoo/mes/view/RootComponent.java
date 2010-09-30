package com.qcadoo.mes.view;

import java.util.Set;

public interface RootComponent extends ContainerComponent<Object> {

    void initialize();

    Set<String> lookupListeners(String path);

    Component<?> lookupComponent(String path);

}
