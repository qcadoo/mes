package com.qcadoo.mes.core.view;

import java.util.Map;

public interface InitializableComponent {

    boolean initializeComponent(Map<String, Component<?>> componentRegistry);

    boolean isInitialized();

}