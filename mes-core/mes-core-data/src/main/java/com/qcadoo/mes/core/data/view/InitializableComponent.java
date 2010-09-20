package com.qcadoo.mes.core.data.view;

import java.util.Map;

public interface InitializableComponent {

    boolean initializeComponent(Map<String, ComponentDefinition<?>> componentRegistry);

    boolean isInitialized();

}