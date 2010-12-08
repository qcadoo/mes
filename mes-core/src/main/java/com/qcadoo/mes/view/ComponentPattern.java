package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.api.ViewDefinitionService;

public interface ComponentPattern {

    boolean initialize();

    void registerViews(ViewDefinitionService viewDefinitionService);

    ComponentState createComponentState();

    Map<String, Object> prepareView(Locale locale);

    String getName();

    String getPath();

}
