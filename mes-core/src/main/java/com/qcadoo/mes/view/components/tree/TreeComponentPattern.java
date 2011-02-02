package com.qcadoo.mes.view.components.tree;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.TreeType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("tree")
public final class TreeComponentPattern extends AbstractComponentPattern {

    private static final String JSP_PATH = "elements/tree.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.Tree";

    private String correspondingView;

    private String correspondingComponent;

    private String nodeLabelExpression;

    public TreeComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new TreeComponentState(getScopeFieldDefinition(), nodeLabelExpression);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        checkScopeFieldDefinition();

        for (ComponentOption option : getOptions()) {
            if ("correspondingView".equals(option.getType())) {
                correspondingView = option.getValue();
            } else if ("correspondingComponent".equals(option.getType())) {
                correspondingComponent = option.getValue();
            } else if ("nodeLabelExpression".equals(option.getType())) {
                nodeLabelExpression = option.getValue();
            } else {
                throw new IllegalStateException("Unknown option for tree: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("correspondingView", correspondingView);
        json.put("correspondingComponent", correspondingComponent);
        json.put("belongsToFieldName", getBelongsToFieldDefinition().getName());

        JSONObject translations = new JSONObject();

        translations.put("newButton", getTranslation("newButton", locale));
        translations.put("editButton", getTranslation("editButton", locale));
        translations.put("deleteButton", getTranslation("deleteButton", locale));
        translations.put("confirmDeleteMessage", getTranslation("confirmDeleteMessage", locale));

        translations.put("header", getTranslationService().translate(getTranslationPath() + ".header", locale));

        translations.put("loading", getTranslationService().translate("commons.loading", locale));

        json.put("translations", translations);

        return json;
    }

    private String getTranslation(final String key, final Locale locale) throws JSONException {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.tree." + key });
        return getTranslationService().translate(codes, locale);
    }

    private void checkScopeFieldDefinition() {
        if (getScopeFieldDefinition() != null) {
            if (TreeType.class.isAssignableFrom(getScopeFieldDefinition().getType().getClass())) {
                return;
            }
        }
        throw new IllegalStateException("Scope field has to be a tree one");
    }

    private FieldDefinition getBelongsToFieldDefinition() {
        if (getScopeFieldDefinition() != null) {
            if (TreeType.class.isAssignableFrom(getScopeFieldDefinition().getType().getClass())) {
                TreeType treeType = (TreeType) getScopeFieldDefinition().getType();
                return treeType.getDataDefinition().getField(treeType.getJoinFieldName());
            }
        }
        throw new IllegalStateException("Scope field has to be a tree one");
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
