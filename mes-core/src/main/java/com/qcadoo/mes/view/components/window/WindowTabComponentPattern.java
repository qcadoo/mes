package com.qcadoo.mes.view.components.window;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonUtils;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

public class WindowTabComponentPattern extends AbstractContainerPattern {

    private static final String JSP_PATH = "containers/windowTab.jsp";

    private static final String JS_OBJECT = "QCD.components.containers.WindowTab";

    private Ribbon ribbon;

    public WindowTabComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new WindowTabComponentState();
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);

        NodeList childNodes = componentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if ("ribbon".equals(child.getNodeName())) {
                setRibbon(RibbonUtils.getInstance().parseRibbon(child, parser, getViewDefinition()));
                break;
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        if (ribbon != null) {
            json.put("ribbon", RibbonUtils.getInstance().translateRibbon(ribbon, locale, this));
        }
        return json;
    }

    public void setRibbon(final Ribbon ribbon) {
        this.ribbon = ribbon;
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
