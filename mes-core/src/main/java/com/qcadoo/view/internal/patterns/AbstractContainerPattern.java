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

package com.qcadoo.view.internal.patterns;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ContainerState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.ContainerPattern;
import com.qcadoo.view.internal.api.InternalViewDefinitionService;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

public abstract class AbstractContainerPattern extends AbstractComponentPattern implements ContainerPattern {

    private final Map<String, ComponentPattern> children = new LinkedHashMap<String, ComponentPattern>();

    public AbstractContainerPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState createComponentState(final ViewDefinitionState viewDefinitionState) {
        ContainerState componentState = (ContainerState) super.createComponentState(viewDefinitionState);

        for (ComponentPattern componentPattern : children.values()) {
            componentState.addChild(componentPattern.createComponentState(viewDefinitionState));
        }

        return componentState;
    }

    @Override
    public final void registerViews(final InternalViewDefinitionService viewDefinitionService) {
        registerComponentViews(viewDefinitionService);

        for (ComponentPattern componentPattern : children.values()) {
            componentPattern.registerViews(viewDefinitionService);
        }
    }

    @Override
    public void initializeAll() {
        super.initializeAll();
        for (ComponentPattern componentPattern : children.values()) {
            componentPattern.initializeAll();
        }
    }

    @Override
    public final void unregisterComponent(final InternalViewDefinitionService viewDefinitionService) {
        super.unregisterComponent(viewDefinitionService);
        for (ComponentPattern componentPattern : children.values()) {
            componentPattern.unregisterComponent(viewDefinitionService);
        }
    }

    @Override
    public final Map<String, ComponentPattern> getChildren() {
        return children;
    }

    public final void addChild(final ComponentPattern child) {
        children.put(child.getName(), child);
    }

    public final void removeChild(final String childName) {
        children.remove(childName);
    }

    @Override
    public final ComponentPattern getChild(final String name) {
        return children.get(name);
    }

    @Override
    public Map<String, Object> prepareView(final Locale locale) {
        Map<String, Object> model = super.prepareView(locale);
        Map<String, Object> childrenModels = new LinkedHashMap<String, Object>();

        for (ComponentPattern child : children.values()) {
            childrenModels.put(child.getName(), child.prepareView(locale));
        }

        model.put("children", childrenModels);

        return model;
    }

    @Override
    public void updateComponentStateListeners(final ViewDefinitionState viewDefinitionState) {
        // TODO masz is this really neccessary?
        super.updateComponentStateListeners(viewDefinitionState);
        for (ComponentPattern child : children.values()) {
            ((AbstractComponentPattern) child).updateComponentStateListeners(viewDefinitionState);
        }
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);

        NodeList childNodes = componentNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("component".equals(child.getNodeName())) {
                addChild(parser.parseComponent(child, this));
            }
        }
    }

    public void parseWithoutChildren(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);
    }

}
