package com.qcadoo.mes.view.xml;

import org.w3c.dom.Node;

import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;

public interface ViewDefinitionParser {

    ComponentOption parseOption(Node option);

    ComponentPattern parseComponent(Node componentNode, ContainerPattern parent);

    ComponentCustomEvent parseCustomEvent(Node listenerNode);

    String getStringAttribute(Node groupNode, String string);

    String getStringNodeContent(Node node);

    Boolean getBooleanAttribute(Node node, String name, boolean defaultValue);

    ViewDefinition parseViewDefinition(Node node, String pluginIdentifier, String name);

}
