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
import com.qcadoo.mes.view.ribbon.RibbonComboBox;
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

        JSONObject translations = new JSONObject();

        addTranslation(translations, "message.noRecordSelected", locale);
        addTranslation(translations, "message.recordAlreadyGenerated", locale);
        addTranslation(translations, "message.recordNotGenerated", locale);
        addTranslation(translations, "message.recordNotCreated", locale);

        json.put("translations", translations);

        return json;
    }

    private void addTranslation(final JSONObject translation, final String key, final Locale locale) throws JSONException {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.ribbon." + key });
        translation.put(key, getTranslationService().translate(codes, locale));
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
                return createNavigationTemplate(parser);
            } else if ("gridNewAndRemoveAction".equals(template)) {
                return createGridNewAndRemoveActionsTemplate(parser);
            } else if ("gridNewCopyAndRemoveAction".equals(template)) {
                return createGridNewCopyAndRemoveActionTemplate(parser);
            } else if ("gridNewAndCopyAction".equals(template)) {
                return createGridNewAndCopyActionTemplate(parser);
            } else if ("formSaveCopyAndRemoveActions".equals(template)) {
                return createFormSaveCopyAndRemoveActionsTemplate(parser);
            } else if ("formSaveAndRemoveActions".equals(template)) {
                return createFormSaveAndRemoveActionsTemplate(parser);
            } else if ("formSaveAction".equals(template)) {
                return createFormSaveActionTemplate(parser);
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
        } else if ("combobox".equals(stringType)) {
            type = RibbonActionItem.Type.COMBOBOX;
        }

        RibbonActionItem item = null;
        if ("bigButtons".equals(stringType) || "smallButtons".equals(stringType)) {
            item = new RibbonComboItem();
        } else if ("combobox".equals(stringType)) {
            item = new RibbonComboBox();
        } else {
            item = new RibbonActionItem();
        }

        item.setIcon(parser.getStringAttribute(itemNode, "icon"));
        item.setName(parser.getStringAttribute(itemNode, "name"));
        item.setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action"), parser));
        item.setType(type);
        String state = parser.getStringAttribute(itemNode, "state");
        if (state != null) {
            if ("enabled".equals(state)) {
                item.setEnabled(true);
            } else if ("disabled".equals(state)) {
                item.setEnabled(false);
            } else {
                throw new IllegalStateException("Unsupported ribbon item state : " + state);
            }
        }
        String message = parser.getStringAttribute(itemNode, "message");
        if (message != null) {
            item.setMessage(message);
        }

        NodeList childNodes = itemNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "script".equals(child.getNodeName())) {
                item.setScript(parser.getStringNodeContent(child));
            }
        }

        if (item instanceof RibbonComboItem) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && !"script".equals(child.getNodeName())) {
                    ((RibbonComboItem) item).addItem(parseRibbonItem(child, parser));
                }
            }
        } else if (item instanceof RibbonComboBox) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && !"script".equals(child.getNodeName())) {
                    if (!"option".equals(child.getNodeName())) {
                        throw new IllegalStateException("ribbon combobox can only have 'option' elements");
                    }
                    ((RibbonComboBox) item).addOption(parser.getStringAttribute(child, "name"));
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

    private RibbonGroup createNavigationTemplate(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonBackAction = new RibbonActionItem();
        ribbonBackAction.setAction(translateRibbonAction("#{window}.performBack", parser));
        ribbonBackAction.setIcon("backIcon24.png");
        ribbonBackAction.setName("back");
        ribbonBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonBackAction);

        return ribbonGroup;
    }

    private RibbonGroup createGridNewAndRemoveActionsTemplate(final ViewDefinitionParser parser) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(parser));
        ribbonGroup.addItem(createGridDeleteAction(parser));
        return ribbonGroup;
    }

    private RibbonGroup createGridNewAndCopyActionTemplate(final ViewDefinitionParser parser) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(parser));
        ribbonGroup.addItem(createGridCopyAction(parser));
        return ribbonGroup;
    }

    private RibbonGroup createGridNewCopyAndRemoveActionTemplate(final ViewDefinitionParser parser) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction(parser));
        ribbonGroup.addItem(createGridCopyAction(parser));
        ribbonGroup.addItem(createGridDeleteAction(parser));
        return ribbonGroup;
    }

    private RibbonActionItem createGridDeleteAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(translateRibbonAction("#{grid}.performDelete;", parser));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonDeleteAction.setEnabled(false);
        ribbonDeleteAction.setMessage("noRecordSelected");
        ribbonDeleteAction
                .setScript("var listener = {onChange: function(selectedRecord) {if (!selectedRecord) {"
                        + "this.setDisableMessage('noRecordSelected');} else {this.setEnabled();}}}; #{grid}.addOnChangeListener(listener);");
        return ribbonDeleteAction;
    }

    private RibbonActionItem createGridCopyAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(translateRibbonAction("#{grid}.performCopy;", parser));
        ribbonCopyAction.setIcon("copyIcon16.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setEnabled(false);
        ribbonCopyAction.setMessage("noRecordSelected");
        ribbonCopyAction
                .setScript("var listener = {onChange: function(selectedRecord) {if (!selectedRecord) {"
                        + "this.setDisableMessage('noRecordSelected');} else {this.setEnabled();}}}; #{grid}.addOnChangeListener(listener);");
        ribbonCopyAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCopyAction;
    }

    private RibbonActionItem createGridNewAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonNewAction = new RibbonActionItem();
        ribbonNewAction.setAction(translateRibbonAction("#{grid}.performNew;", parser));
        ribbonNewAction.setIcon("newIcon24.png");
        ribbonNewAction.setName("new");
        ribbonNewAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonNewAction;
    }

    private RibbonGroup createFormSaveCopyAndRemoveActionsTemplate(final ViewDefinitionParser parser) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction(parser));
        ribbonGroup.addItem(createFormSaveAndBackAction(parser));
        ribbonGroup.addItem(createFormCopyAction(parser));
        ribbonGroup.addItem(createFormCancelAction(parser));
        ribbonGroup.addItem(createFormDeleteAction(parser));
        return ribbonGroup;
    }

    private RibbonGroup createFormSaveAndRemoveActionsTemplate(final ViewDefinitionParser parser) {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction(parser));
        ribbonGroup.addItem(createFormSaveAndBackAction(parser));
        ribbonGroup.addItem(createFormCancelAction(parser));
        ribbonGroup.addItem(createFormDeleteAction(parser));
        return ribbonGroup;
    }

    private RibbonGroup createFormSaveActionTemplate(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(translateRibbonAction("#{form}.performSave; #{window}.performBack", parser));
        ribbonSaveAction.setIcon("saveBackIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonSaveAction);

        return ribbonGroup;
    }

    private RibbonActionItem createFormDeleteAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(translateRibbonAction("#{form}.performDelete; #{window}.performBack", parser));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonDeleteAction;
    }

    private RibbonActionItem createFormCancelAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonCancelAction = new RibbonActionItem();
        ribbonCancelAction.setAction(translateRibbonAction("#{form}.performCancel;", parser));
        ribbonCancelAction.setIcon("cancelIcon16.png");
        ribbonCancelAction.setName("cancel");
        ribbonCancelAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCancelAction;
    }

    private RibbonActionItem createFormCopyAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(translateRibbonAction("#{form}.performCopy;", parser));
        ribbonCopyAction.setIcon("copyIcon24.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonCopyAction;
    }

    private RibbonActionItem createFormSaveAndBackAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonSaveBackAction = new RibbonActionItem();
        ribbonSaveBackAction.setAction(translateRibbonAction("#{form}.performSave; #{window}.performBack;", parser));
        ribbonSaveBackAction.setIcon("saveBackIcon24.png");
        ribbonSaveBackAction.setName("saveBack");
        ribbonSaveBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveBackAction;
    }

    private RibbonActionItem createFormSaveAction(final ViewDefinitionParser parser) {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(translateRibbonAction("#{form}.performSave;", parser));
        ribbonSaveAction.setIcon("saveIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveAction;
    }

}
