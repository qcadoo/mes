package com.qcadoo.mes.view.xml;

import org.w3c.dom.Node;

import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;

public interface ViewDefinitionParser {

    ComponentOption parseOption(Node option);

    ComponentPattern parseComponent(Node componentNode, ContainerPattern parent);

    ComponentCustomEvent parseCustomEvent(Node listenerNode);

    String getStringAttribute(Node groupNode, String string);

    String getStringNodeContent(Node node);

    Boolean getBooleanAttribute(final Node node, final String name, final boolean defaultValue);

}
