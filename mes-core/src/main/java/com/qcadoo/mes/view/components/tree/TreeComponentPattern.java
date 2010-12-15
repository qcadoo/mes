package com.qcadoo.mes.view.components.tree;

import org.json.JSONException;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("tree")
public final class TreeComponentPattern extends AbstractComponentPattern {

    private static final String JSP_PATH = "elements/tree.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.Tree";

    private FieldDefinition belongsToFieldDefinition;

    public TreeComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new TreeComponentState(belongsToFieldDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        getBelongsToFieldDefinition();
    }

    private void getBelongsToFieldDefinition() {
        if (getScopeFieldDefinition() != null) {
            if (HasManyType.class.isAssignableFrom(getScopeFieldDefinition().getType().getClass())) {
                HasManyType hasManyType = (HasManyType) getScopeFieldDefinition().getType();
                belongsToFieldDefinition = hasManyType.getDataDefinition().getField(hasManyType.getJoinFieldName());
            } else {
                throw new IllegalStateException("Scope field for grid be a hasMany one");
            }
        }
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
