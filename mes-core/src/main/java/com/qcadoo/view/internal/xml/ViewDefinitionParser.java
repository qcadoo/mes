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

package com.qcadoo.view.internal.xml;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentOption;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.ContainerPattern;
import com.qcadoo.view.internal.ViewDefinition;
import com.qcadoo.view.internal.internal.ComponentCustomEvent;
import com.qcadoo.view.internal.ribbon.RibbonGroup;

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
