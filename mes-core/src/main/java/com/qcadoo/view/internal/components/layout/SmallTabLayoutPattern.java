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

package com.qcadoo.view.internal.components.layout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.components.layout.SmallTabLayoutState;
import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.ViewComponent;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

@ViewComponent("smallTabLayout")
public class SmallTabLayoutPattern extends AbstractLayoutPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.SmallTabLayout";

    private static final String JSP_PATH = "containers/layout/smallTabLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/smallTabLayout.js";

    // private String label;
    private List<SmallTabLayoutPatternTab> tabs = new LinkedList<SmallTabLayoutPatternTab>();

    public SmallTabLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);

        NodeList childNodes = componentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Preconditions.checkState("tabElement".equals(child.getNodeName()), "gridlayout can contains only tabElements");
            String tabName = parser.getStringAttribute(child, "name");

            SmallTabLayoutPatternTab tab = new SmallTabLayoutPatternTab(tabName);

            NodeList elementComponentNodes = child.getChildNodes();
            for (int elementComponentNodesIter = 0; elementComponentNodesIter < elementComponentNodes.getLength(); elementComponentNodesIter++) {
                Node elementComponentNode = elementComponentNodes.item(elementComponentNodesIter);
                if (elementComponentNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Preconditions.checkState("component".equals(elementComponentNode.getNodeName()),
                        "layoutElement can contains only components");
                ComponentPattern elementComponent = parser.parseComponent(elementComponentNode, this);
                this.addChild(elementComponent);
                tab.addComponent(elementComponent);
            }

            tabs.add(tab);
        }
    }

    @Override
    public final Map<String, Object> prepareView(final Locale locale) {
        Map<String, Object> model = super.prepareView(locale);
        model.put("tabs", tabs);
        Map<String, String> translations = new HashMap<String, String>();
        for (SmallTabLayoutPatternTab tab : tabs) {
            translations.put(tab.getName(),
                    getTranslationService().translate(getTranslationPath() + "." + getName() + "." + tab.getName(), locale));
        }
        model.put("tabTranslations", translations);
        return model;
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new SmallTabLayoutState(tabs);
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }

}
