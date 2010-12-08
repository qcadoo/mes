package com.qcadoo.mes.view.components.lookup;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.FieldComponentPattern;
import com.qcadoo.mes.view.components.WindowComponentPattern;
import com.qcadoo.mes.view.components.grid.GridComponentPattern;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.ribbon.RibbonActionItem.Type;
import com.qcadoo.mes.view.ribbon.RibbonGroup;

@ViewComponent("lookup")
public final class LookupComponentPattern extends FieldComponentPattern {

    private static final String JSP_PATH = "elements/lookup.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.Lookup";

    private String expression;

    private String fieldCode;

    private ViewDefinition lookupViewDefinition;

    public LookupComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new LookupComponentState();
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

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("expression".equals(option.getType())) {
                expression = option.getValue();
            } else if ("fieldCode".equals(option.getType())) {
                fieldCode = option.getValue();
            }
        }

        String viewName = getViewName();

        lookupViewDefinition = new ViewDefinitionImpl(viewName, getViewDefinition().getPluginIdentifier(), getDataDefinition(),
                false, getTranslationService());

        WindowComponentPattern window = createWindowComponentPattern(lookupViewDefinition);

        GridComponentPattern grid = createGridComponentPattern(lookupViewDefinition, window);

        for (ComponentOption option : getOptions()) {
            if ("expression".equals(option.getType())) {
                continue;
            } else if ("fieldCode".equals(option.getType())) {
                continue;
            } else if ("orderable".equals(option.getType())) {
                Map<String, String> newAttributes = new HashMap<String, String>();
                newAttributes.put("value", option.getValue() + ",lookupCode");
                option = new ComponentOption("orderable", newAttributes);
                grid.addOption(option);
            } else if ("searchable".equals(option.getType())) {
                Map<String, String> newAttributes = new HashMap<String, String>();
                newAttributes.put("value", option.getValue() + ",lookupCode");
                option = new ComponentOption("searchable", newAttributes);
                grid.addOption(option);
            } else {
                grid.addOption(option);
            }
        }

        window.addChild(grid);

        ((ViewDefinitionImpl) lookupViewDefinition).addComponentPattern(window);
        ((ViewDefinitionImpl) lookupViewDefinition).initialize();
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("viewName", getViewName());

        JSONObject translations = new JSONObject();

        List<String> codes = new LinkedList<String>();
        codes.add(getTranslationPath() + ".label.focus");
        codes.add(getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), getFieldDefinition().getName())
                + ".label.focus");
        translations.put("labelOnFocus", getTranslationService().translate(codes, locale));

        json.put("translations", translations);

        return json;
    }

    @Override
    protected void registerComponentViews(final ViewDefinitionService viewDefinitionService) {
        viewDefinitionService.save(lookupViewDefinition);
    }

    private GridComponentPattern createGridComponentPattern(final ViewDefinition lookupViewDefinition,
            final WindowComponentPattern window) {
        ComponentDefinition gridComponentDefinition = new ComponentDefinition();
        gridComponentDefinition.setName("grid");
        gridComponentDefinition.setTranslationService(getTranslationService());
        gridComponentDefinition.setViewDefinition(lookupViewDefinition);
        gridComponentDefinition.setParent(window);
        // gridComponentDefinition.setSourceFieldPath(sourceFieldPath)

        GridComponentPattern grid = new GridComponentPattern(gridComponentDefinition);
        grid.addOption(new ComponentOption("lookup", ImmutableMap.of("value", "true")));
        grid.addOption(new ComponentOption("fullscreen", ImmutableMap.of("value", "true")));
        grid.addOption(new ComponentOption("orderable", ImmutableMap.of("value", "lookupCode")));
        grid.addOption(new ComponentOption("searchable", ImmutableMap.of("value", "lookupCode")));
        grid.addOption(createLookupValueColumn());
        grid.addOption(createLookupCodeColumn());

        return grid;
    }

    private ComponentOption createLookupValueColumn() {
        Map<String, String> valueColumnOptions = new HashMap<String, String>();
        valueColumnOptions.put("name", "lookupValue");
        valueColumnOptions.put("expression", expression);
        valueColumnOptions.put("hidden", "true");
        return new ComponentOption("column", valueColumnOptions);
    }

    private ComponentOption createLookupCodeColumn() {
        Map<String, String> codeVisibleColumnOptions = new HashMap<String, String>();
        codeVisibleColumnOptions.put("name", "lookupCode");
        codeVisibleColumnOptions.put("fields", fieldCode);
        codeVisibleColumnOptions.put("hidden", "false");
        codeVisibleColumnOptions.put("link", "true");
        return new ComponentOption("column", codeVisibleColumnOptions);
    }

    private WindowComponentPattern createWindowComponentPattern(final ViewDefinition lookupViewDefinition) {
        ComponentDefinition windowComponentDefinition = new ComponentDefinition();
        windowComponentDefinition.setName("window");
        windowComponentDefinition.setTranslationService(getTranslationService());
        windowComponentDefinition.setViewDefinition(lookupViewDefinition);

        WindowComponentPattern window = new WindowComponentPattern(windowComponentDefinition);
        window.addOption(new ComponentOption("fixedHeight", ImmutableMap.of("value", "true")));
        window.addOption(new ComponentOption("header", ImmutableMap.of("value", "false")));
        window.setRibbon(createRibbon());

        return window;
    }

    private Ribbon createRibbon() {
        RibbonActionItem ribbonActionItem = new RibbonActionItem();
        ribbonActionItem.setName("select");
        ribbonActionItem.setIcon("acceptIcon24.png");
        ribbonActionItem.setAction("#{window.grid}.performLinkClicked();");
        ribbonActionItem.setType(Type.BIG_BUTTON);

        RibbonActionItem ribbonCancelActionItem = new RibbonActionItem();
        ribbonCancelActionItem.setName("cancel");
        ribbonCancelActionItem.setIcon("cancelIcon24.png");
        ribbonCancelActionItem.setAction("#{window}.performCloseWindow");
        ribbonCancelActionItem.setType(Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonActionItem);
        ribbonGroup.addItem(ribbonCancelActionItem);

        Ribbon ribbon = new Ribbon();
        ribbon.addGroup(ribbonGroup);

        return ribbon;
    }

    private String getViewName() {
        return getViewDefinition().getName() + getPath() + ".lookup";
    }

}
