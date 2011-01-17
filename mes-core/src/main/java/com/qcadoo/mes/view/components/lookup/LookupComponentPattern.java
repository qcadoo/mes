package com.qcadoo.mes.view.components.lookup;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
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
        if (getScopeFieldDefinition() != null) {
            String joinFieldName = ((HasManyType) getScopeFieldDefinition().getType()).getJoinFieldName();
            FieldDefinition fieldDefinition = getDataDefinition().getField(joinFieldName);
            return new LookupComponentState(fieldDefinition, fieldCode, expression);
        } else {
            return new LookupComponentState(null, fieldCode, expression);
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

    @Override
    protected void initializeComponent() throws JSONException {

        System.out.println(" -- INITIALIZE LOOKUP");

        for (ComponentOption option : getOptions()) {
            if ("expression".equals(option.getType())) {
                expression = option.getValue();
            } else if ("fieldCode".equals(option.getType())) {
                fieldCode = option.getValue();
            }
        }

        checkState(hasText(fieldCode), "Missing fieldCode for lookup");
        checkState(hasText(expression), "Missing expression for lookup");

        String viewName = getViewName();

        DataDefinition dataDefinition = getDataDefinition();

        if (getScopeFieldDefinition() != null) {
            dataDefinition = getScopeFieldDefinition().getDataDefinition();
        }

        lookupViewDefinition = new ViewDefinitionImpl(viewName, getViewDefinition().getPluginIdentifier(), dataDefinition, false,
                getTranslationService());

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

        System.out.println("------>");
        System.out.println(getFieldDefinition());
        System.out.println(getDataDefinition());

        if (getFieldDefinition() != null) {
            codes.add(getTranslationService().getEntityFieldBaseMessageCode(getFieldDefinition().getDataDefinition(),
                    getFieldDefinition().getName())
                    + ".label.focus");
        } else {
            codes.add(getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), getName()) + ".label.focus");
        }

        translations.put("labelOnFocus", getTranslationService().translate(codes, locale));

        translations.put(
                "noMatchError",
                getTranslationService().translate(
                        Arrays.asList(new String[] { getTranslationPath() + ".noMatchError", "core.lookup.noMatchError" }),
                        locale));
        translations.put(
                "moreTahnOneMatchError",
                getTranslationService().translate(
                        Arrays.asList(new String[] { getTranslationPath() + ".moreTahnOneMatchError",
                                "core.lookup.moreTahnOneMatchError" }), locale));
        translations.put(
                "noResultsInfo",
                getTranslationService().translate(
                        Arrays.asList(new String[] { getTranslationPath() + ".noResultsInfo", "core.lookup.noResultsInfo" }),
                        locale));
        translations.put(
                "tooManyResultsInfo",
                getTranslationService().translate(
                        Arrays.asList(new String[] { getTranslationPath() + ".tooManyResultsInfo",
                                "core.lookup.tooManyResultsInfo" }), locale));

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

        if (getScopeFieldDefinition() != null) {
            gridComponentDefinition.setSourceFieldPath(getScopeFieldDefinition().getName());
        }

        GridComponentPattern grid = new GridComponentPattern(gridComponentDefinition);
        grid.addOption(new ComponentOption("lookup", ImmutableMap.of("value", "true")));
        grid.addOption(new ComponentOption("fullscreen", ImmutableMap.of("value", "true")));
        grid.addOption(new ComponentOption("orderable", ImmutableMap.of("value", "lookupCode")));
        grid.addOption(new ComponentOption("order", ImmutableMap.of("column", "lookupCode", "direction", "asc")));
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
        return getViewDefinition().getName() + "." + getFunctionalPath() + ".lookup";
    }

}
