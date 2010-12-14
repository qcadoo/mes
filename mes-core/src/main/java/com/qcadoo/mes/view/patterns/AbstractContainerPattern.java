package com.qcadoo.mes.view.patterns;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

public abstract class AbstractContainerPattern extends AbstractComponentPattern implements ContainerPattern {

    private final Map<String, ComponentPattern> children = new LinkedHashMap<String, ComponentPattern>();

    public AbstractContainerPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public final ComponentState createComponentState(final ViewDefinitionState viewDefinitionState) {
        ContainerState componentState = (ContainerState) super.createComponentState(viewDefinitionState);

        for (ComponentPattern componentPattern : children.values()) {
            componentState.addChild(componentPattern.createComponentState(viewDefinitionState));
        }

        return componentState;
    }

    @Override
    public final void registerViews(final ViewDefinitionService viewDefinitionService) {
        registerComponentViews(viewDefinitionService);

        for (ComponentPattern componentPattern : children.values()) {
            componentPattern.registerViews(viewDefinitionService);
        }
    }

    @Override
    public final Map<String, ComponentPattern> getChildren() {
        return children;
    }

    public final void addChild(final ComponentPattern child) {
        children.put(child.getName(), child);
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

}
