package com.qcadoo.mes.view.components.awesomeDynamicList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.layout.FlowLayoutPattern;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

@ViewComponent("awesomeDynamicList")
public class AwesomeDynamicListPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.AwesomeDynamicList";

    private static final String JSP_PATH = "elements/awesomeDynamicList.jsp";

    private final FormComponentPattern innerFormPattern;

    private ComponentPattern headerFormPattern;

    private boolean hasButtons = true;

    private boolean hasBorder = true;

    public AwesomeDynamicListPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
        ComponentDefinition formComponentDefinition = new ComponentDefinition();
        formComponentDefinition.setName("innerForm_@innerFormId");
        formComponentDefinition.setFieldPath(null);
        formComponentDefinition.setSourceFieldPath(null);
        formComponentDefinition.setTranslationService(getTranslationService());
        formComponentDefinition.setViewDefinition(getViewDefinition());
        formComponentDefinition.setParent(this);
        innerFormPattern = new FormComponentPattern(formComponentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        initializeComponent(innerFormPattern);
        if (headerFormPattern != null) {
            initializeComponent(headerFormPattern);
        }
        for (ComponentOption option : getOptions()) {
            if ("hasButtons".equals(option.getType())) {
                hasButtons = Boolean.parseBoolean(option.getValue());
            } else if ("hasBorder".equals(option.getType())) {
                hasBorder = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for AwesomeDynamicList: " + option.getType());
            }
        }
    }

    @Override
    protected void registerComponentViews(final ViewDefinitionService viewDefinitionService) {
        innerFormPattern.registerViews(viewDefinitionService);
        if (headerFormPattern != null) {
            headerFormPattern.registerViews(viewDefinitionService);
        }
    }

    private void initializeComponent(final ComponentPattern component) {
        component.initialize();
        if (component instanceof ContainerPattern) {
            ContainerPattern container = (ContainerPattern) component;
            for (ComponentPattern kids : container.getChildren().values()) {
                initializeComponent(kids);
            }
        }
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);
        NodeList childNodes = componentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if ("components".equals(child.getNodeName())) {
                innerFormPattern.parse(child, parser);
            } else if ("header".equals(child.getNodeName())) {
                ComponentDefinition formComponentDefinition = new ComponentDefinition();
                formComponentDefinition.setName("header");
                formComponentDefinition.setFieldPath(null);
                formComponentDefinition.setSourceFieldPath(null);
                formComponentDefinition.setTranslationService(getTranslationService());
                formComponentDefinition.setViewDefinition(getViewDefinition());
                formComponentDefinition.setParent(this);
                headerFormPattern = new FlowLayoutPattern(formComponentDefinition);
                headerFormPattern.parse(child, parser);
            }
        }
    }

    public FormComponentPattern getFormComponentPattern() {
        return innerFormPattern;
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("hasButtons", hasButtons);
        json.put("hasBorder", hasBorder);
        return json;
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("innerForm", innerFormPattern.prepareView(locale));
        options.put("hasBorder", hasBorder);
        options.put("hasButtons", hasButtons);
        if (headerFormPattern != null) {
            options.put("header", headerFormPattern.prepareView(locale));
        }
        return options;
    }

    @Override
    protected String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    protected String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    protected String getJsObjectName() {
        return JS_OBJECT;
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new AwesomeDynamicListState(innerFormPattern, headerFormPattern);
    }

}
