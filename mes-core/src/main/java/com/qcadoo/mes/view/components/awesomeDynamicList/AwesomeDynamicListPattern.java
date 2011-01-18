package com.qcadoo.mes.view.components.awesomeDynamicList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

@ViewComponent("awesomeDynamicList")
public class AwesomeDynamicListPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.AwesomeDynamicList";

    private static final String JSP_PATH = "elements/awesomeDynamicList.jsp";

    private FieldDefinition belongsToFieldDefinition;

    private FormComponentPattern innerFormPattern;

    public AwesomeDynamicListPattern(ComponentDefinition componentDefinition) {
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
        getBelongsToFieldDefinition();
        System.out.println(" -- INITIALIZE LIST BEGIN");
        initializeComponent(innerFormPattern);
        // innerFormPattern.initialize();
        // for (ComponentPattern formComponent : innerFormPattern.getChildren().values()) {
        // formComponent.initialize();
        // if (formComponent instanceof ContainerPattern) {
        // ContainerPattern formComponentConteiner = (ContainerPattern) formComponent;
        // for (ComponentPattern formComponentConteinerKid : formComponentConteiner.getChildren().values()) {
        // formComponentConteinerKid.initialize();
        // }
        // }
        // }
        System.out.println(" -- INITIALIZE LIST END");
    }

    private void initializeComponent(ComponentPattern component) {
        component.initialize();
        if (component instanceof ContainerPattern) {
            ContainerPattern container = (ContainerPattern) component;
            for (ComponentPattern kids : container.getChildren().values()) {
                initializeComponent(kids);
            }
        }
    }

    private void getBelongsToFieldDefinition() {
        if (getScopeFieldDefinition() != null) {
            if (HasManyType.class.isAssignableFrom(getScopeFieldDefinition().getType().getClass())) {
                HasManyType hasManyType = (HasManyType) getScopeFieldDefinition().getType();
                belongsToFieldDefinition = hasManyType.getDataDefinition().getField(hasManyType.getJoinFieldName());
            } else {
                throwIllegalStateException("Scope field for grid be a hasMany one");
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
            }
        }
    }

    public FormComponentPattern getFormComponentPattern() {
        return innerFormPattern;
    }

    // @Override
    // public final ComponentState createComponentState(final ViewDefinitionState viewDefinitionState) {
    // ContainerState componentState = (ContainerState) super.createComponentState(viewDefinitionState);
    // return componentState;
    // }

    // @Override
    // public Map<String, Object> prepareView(final Locale locale) {
    // Map<String, Object> model = super.prepareView(locale);
    // Map<String, Object> childrenModels = new LinkedHashMap<String, Object>();
    //
    // model.put("children", childrenModels);
    //
    // return model;
    // }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("innerForm", innerFormPattern.prepareView(locale));
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
        ComponentState listState = new AwesomeDynamicListState(belongsToFieldDefinition, innerFormPattern);

        return listState;
    }

    private void throwIllegalStateException(final String message) {
        throw new IllegalStateException(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "#"
                + getPath() + ": " + message);
    }

}
