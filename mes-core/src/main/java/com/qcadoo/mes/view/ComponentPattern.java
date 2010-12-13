package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Node;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

public interface ComponentPattern {

    boolean initialize();

    void registerViews(ViewDefinitionService viewDefinitionService);

    ComponentState createComponentState(ViewDefinitionState viewDefinitionState);

    Map<String, Object> prepareView(Locale locale);

    String getName();

    String getPath();

    String getFunctionalPath();

    void parse(Node componentNode, ViewDefinitionParser parser);

}
