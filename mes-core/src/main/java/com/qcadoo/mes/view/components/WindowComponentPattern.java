package com.qcadoo.mes.view.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.ribbon.RibbonGroup;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

@ViewComponent("window")
public final class WindowComponentPattern extends AbstractContainerPattern {

    private static final String JSP_PATH = "containers/window.jsp";

    private static final String JS_OBJECT = "QCD.components.containers.Window";

    private boolean header = true;

    private boolean fixedHeight = false;

    private Ribbon ribbon;

    public WindowComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new EmptyContainerState();
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("fixedHeight".equals(option.getType())) {
                fixedHeight = Boolean.parseBoolean(option.getValue());
            } else if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for window: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("fixedHeight", fixedHeight);
        json.put("header", header);
        if (ribbon != null) {
            json.put("ribbon", getJsRibbon(locale));
        }

        return json;
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("header", header);
        return options;
    }

    private JSONObject getJsRibbon(final Locale locale) throws JSONException {
        JSONObject json = ribbon.getAsJson();

        for (int i = 0; i < json.getJSONArray("groups").length(); i++) {
            JSONObject group = json.getJSONArray("groups").getJSONObject(i);
            group.put("label", getTranslationService().translate(getTranslationCodes(group.getString("name")), locale));
            translateRibbonItems(group, group.getString("name") + ".", locale);
        }

        return json;
    }

    private void translateRibbonItems(final JSONObject owner, final String prefix, final Locale locale) throws JSONException {
        if (owner.has("items")) {
            for (int j = 0; j < owner.getJSONArray("items").length(); j++) {
                JSONObject item = owner.getJSONArray("items").getJSONObject(j);

                String label = getTranslationService().translate(getTranslationCodes(prefix + item.getString("name")), locale);
                item.put("label", label);
                translateRibbonItems(item, prefix + item.getString("name") + ".", locale);
            }
        }
    }

    private List<String> getTranslationCodes(final String key) {
        return Arrays.asList(new String[] { getTranslationPath() + ".ribbon." + key, "core.ribbon." + key });
    }

    @Override
    public void parse(final Node componentNode, final ViewDefinitionParser parser) {
        super.parse(componentNode, parser);

        NodeList childNodes = componentNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("ribbon".equals(child.getNodeName())) {
                setRibbon(parseRibbon(child, parser));
                break;
            }
        }

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

    private Ribbon parseRibbon(final Node ribbonNode, final ViewDefinitionParser parser) {
        Ribbon ribbon = new Ribbon();

        NodeList childNodes = ribbonNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("group".equals(child.getNodeName())) {
                ribbon.addGroup(parseRibbonGroup(child, parser));
            }
        }

        return ribbon;
    }

    private RibbonGroup parseRibbonGroup(final Node groupNode, final ViewDefinitionParser parser) {
        String template = parser.getStringAttribute(groupNode, "template");

        if (template != null) {
            if ("navigation".equals(template)) {
                return createNavigationTemplate();
            } else if ("gridActions".equals(template)) {
                return createGridActionsTemplate();
            } else if ("gridNewAction".equals(template)) {
                return createGridNewActionTemplate();
            } else if ("formActions".equals(template)) {
                return createFormActionsTemplate();
            } else if ("formSaveActions".equals(template)) {
                return createFormSaveActionsTemplate();
            } else if ("formSaveAction".equals(template)) {
                return createFormSaveActionTemplate();
            } else {
                throw new IllegalStateException("Unsupported ribbon template : " + template);
            }
        } else {
            RibbonGroup ribbonGroup = new RibbonGroup();
            ribbonGroup.setName(parser.getStringAttribute(groupNode, "name"));

            NodeList childNodes = groupNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    ribbonGroup.addItem(parseRibbonItem(child, parser));
                }
            }

            return ribbonGroup;
        }

    }

    private RibbonActionItem parseRibbonItem(final Node itemNode, final ViewDefinitionParser parser) {
        String stringType = itemNode.getNodeName();

        RibbonActionItem.Type type = null;
        if ("bigButtons".equals(stringType) || "bigButton".equals(stringType)) {
            type = RibbonActionItem.Type.BIG_BUTTON;
        } else if ("smallButtons".equals(stringType) || "smallButton".equals(stringType)) {
            type = RibbonActionItem.Type.SMALL_BUTTON;
        }

        RibbonActionItem item = null;
        if ("bigButtons".equals(stringType) || "smallButtons".equals(stringType)) {
            item = new RibbonComboItem();
        } else {
            item = new RibbonActionItem();
        }

        item.setIcon(parser.getStringAttribute(itemNode, "icon"));
        item.setName(parser.getStringAttribute(itemNode, "name"));
        item.setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action"), parser));
        item.setType(type);

        if (item instanceof RibbonComboItem) {
            NodeList childNodes = itemNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    ((RibbonComboItem) item).addItem(parseRibbonItem(child, parser));
                }
            }
        } else {
            (item).setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action"), parser));
        }

        return item;
    }

    private String translateRibbonAction(final String action, final ViewDefinitionParser parser) {
        if (action == null) {
            return null;
        }

        Pattern p = Pattern.compile("#\\{([^\\}]+)\\}");
        Matcher m = p.matcher(action);

        String translateAction = action;

        while (m.find()) {
            ComponentPattern actionComponentPattern = getViewDefinition().getComponentByReference(m.group(1));

            if (actionComponentPattern == null) {
                throw new IllegalStateException("Cannot find action component for " + getTranslationPath() + " : " + action
                        + " [" + m.group(1) + "]");
            }

            translateAction = translateAction.replace("#{" + m.group(1) + "}", "#{" + actionComponentPattern.getPath() + "}");
        }

        return translateAction;
    }

    private RibbonGroup createNavigationTemplate() {
        RibbonActionItem ribbonBackAction = new RibbonActionItem();
        ribbonBackAction.setAction("#{window}.performBack");
        ribbonBackAction.setIcon("backIcon24.png");
        ribbonBackAction.setName("back");
        ribbonBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonBackAction);

        return ribbonGroup;
    }

    private RibbonGroup createGridActionsTemplate() {
        RibbonGroup ribbonGroup = createGridNewActionTemplate();

        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction("#{grid}.performDelete;");
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        ribbonGroup.addItem(ribbonDeleteAction);

        return ribbonGroup;
    }

    private RibbonGroup createGridNewActionTemplate() {
        RibbonActionItem ribbonNewAction = new RibbonActionItem();
        ribbonNewAction.setAction("#{grid}.performNew;");
        ribbonNewAction.setIcon("newIcon24.png");
        ribbonNewAction.setName("new");
        ribbonNewAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonNewAction);

        return ribbonGroup;
    }

    private RibbonGroup createFormActionsTemplate() {
        RibbonGroup ribbonGroup = createFormSaveActionsTemplate();

        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction("#{form}.performDelete; #{window}.performBack");
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);

        ribbonGroup.addItem(ribbonDeleteAction);

        return ribbonGroup;
    }

    private RibbonGroup createFormSaveActionsTemplate() {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction("#{form}.performSave;");
        ribbonSaveAction.setIcon("saveIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonActionItem ribbonSaveBackAction = new RibbonActionItem();
        ribbonSaveBackAction.setAction("#{form}.performSave; #{window}.performBack");
        ribbonSaveBackAction.setIcon("saveBackIcon24.png");
        ribbonSaveBackAction.setName("saveBack");
        ribbonSaveBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonActionItem ribbonCancelAction = new RibbonActionItem();
        ribbonCancelAction.setAction("#{form}.performCancel;");
        ribbonCancelAction.setIcon("cancelIcon16.png");
        ribbonCancelAction.setName("cancel");
        ribbonCancelAction.setType(RibbonActionItem.Type.SMALL_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonSaveAction);
        ribbonGroup.addItem(ribbonSaveBackAction);
        ribbonGroup.addItem(ribbonCancelAction);

        return ribbonGroup;
    }

    private RibbonGroup createFormSaveActionTemplate() {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction("#{form}.performSave; #{window}.performBack");
        ribbonSaveAction.setIcon("saveBackIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonSaveAction);

        return ribbonGroup;
    }

}
