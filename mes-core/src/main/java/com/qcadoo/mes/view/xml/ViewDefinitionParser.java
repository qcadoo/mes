/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.xml;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;
import com.qcadoo.mes.view.ribbon.RibbonGroup;

public interface ViewDefinitionParser {

    ComponentOption parseOption(Node option);

    ComponentPattern parseComponent(Node componentNode, ContainerPattern parent);

    ComponentDefinition getComponentDefinition(Node componentNode, ContainerPattern parent, ViewDefinition viewDefinition);

    ComponentCustomEvent parseCustomEvent(Node listenerNode);

    String getStringAttribute(Node groupNode, String string);

    String getStringNodeContent(Node node);

    Boolean getBooleanAttribute(Node node, String name, boolean defaultValue);

    List<Node> geElementChildren(Node node);

    List<ViewDefinition> parseViewXml(final Resource viewXml);

    List<ViewExtension> getViewExtensionNodes(InputStream resource, String tagType);

    RibbonGroup parseRibbonGroup(Node groupNode, ViewDefinition viewDefinition);

}
