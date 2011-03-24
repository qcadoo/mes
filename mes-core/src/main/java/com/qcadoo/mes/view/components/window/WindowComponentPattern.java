/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.components.window;

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
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.ribbon.RibbonComboBox;
import com.qcadoo.mes.view.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.ribbon.RibbonGroup;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

//@ViewComponent("window")
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
        return new WindowComponentState(this);
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

    public JSONObject translateRibbon(final Ribbon ribbon, final Locale locale) throws JSONException {
        JSONObject json = ribbon.getAsJson();

        for (int i = 0; i < json.getJSONArray("groups").length(); i++) {
            JSONObject group = json.getJSONArray("groups").getJSONObject(i);
            group.put("label", getTranslationService().translate(getTranslationCodes(group.getString("name")), locale));
            translateRibbonItems(group, group.getString("name") + ".", locale);
        }

        return json;
    }

    private JSONObject getJsRibbon(final Locale locale) throws JSONException {
        return translateRibbon(ribbon, locale);
    }

    private void translateRibbonItems(final JSONObject owner, final String prefix, final Locale locale) throws JSONException {
        if (owner.has("items")) {
            for (int j = 0; j < owner.getJSONArray("items").length(); j++) {
                JSONObject item = owner.getJSONArray("items").getJSONObject(j);

                String label = getTranslationService().translate(getTranslationCodes(prefix + item.getString("name")), locale);
                item.put("label", label);

                if (item.has("script")) {
                    String script = item.getString("script");
                    if (script != null) {
                        item.put("script", prepareScript(script, locale));
                    }
                }

                if (item.has("message")) {
                    String message = item.getString("message");
                    if (message.contains(".")) {
                        message = getTranslationService().translate(message, locale);
                    } else {
                        message = getTranslationService().translate("core.message." + message, locale);
                    }
                    item.put("message", prepareScript(message, locale));
                }

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

    public Ribbon getRibbonCopy() {
        return ribbon.getCopy();
    }

    private Ribbon parseRibbon(final Node ribbonNode, final ViewDefinitionParser parser) {
        Ribbon windowRibbon = new Ribbon();

        NodeList childNodes = ribbonNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("group".equals(child.getNodeName())) {
                windowRibbon.addGroup(parseRibbonGroup(child, parser));
            }
        }

        return windowRibbon;
    }

    private RibbonGroup parseRibbonGroup(final Node groupNode, final ViewDefinitionParser parser) {
        String template = parser.getStringAttribute(groupNode, "template");

        if (template != null) {
            if ("navigation".equals(template)) {
                return createNavigationTemplate();
            } else if ("gridNewAndRemoveAction".equals(template)) {
                return createGridNewAndRemoveActionsTemplate();
            } else if ("gridNewCopyAndRemoveAction".equals(template)) {
                return createGridNewCopyAndRemoveActionTemplate();
            } else if ("gridNewAndCopyAction".equals(template)) {
                return createGridNewAndCopyActionTemplate();
            } else if ("formSaveCopyAndRemoveActions".equals(template)) {
                return createFormSaveCopyAndRemoveActionsTemplate();
            } else if ("formSaveAndRemoveActions".equals(template)) {
                return createFormSaveAndRemoveActionsTemplate();
            } else if ("formSaveAndBackAndRemoveActions".equals(template)) {
                return createFormSaveAndBackAndRemoveActionsTemplate();
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
        } else if ("combobox".equals(stringType)) {
            type = RibbonActionItem.Type.COMBOBOX;
        } else if ("smallEmptySpace".equals(stringType)) {
            type = RibbonActionItem.Type.SMALL_EMPTY_SPACE;
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
        item.setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action")));
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
        } else {
            item.setEnabled(true);
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
            (item).setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action")));
        }

        return item;
    }

    private String translateRibbonAction(final String action) {
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
        ribbonBackAction.setAction(translateRibbonAction("#{window}.performBack"));
        ribbonBackAction.setIcon("backIcon24.png");
        ribbonBackAction.setName("back");
        ribbonBackAction.setEnabled(true);
        ribbonBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);

        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("navigation");
        ribbonGroup.addItem(ribbonBackAction);

        return ribbonGroup;
    }

    private RibbonGroup createGridNewAndRemoveActionsTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction());
        ribbonGroup.addItem(createGridDeleteAction());
        return ribbonGroup;
    }

    private RibbonGroup createGridNewAndCopyActionTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction());
        ribbonGroup.addItem(createGridCopyAction());
        return ribbonGroup;
    }

    private RibbonGroup createGridNewCopyAndRemoveActionTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createGridNewAction());
        ribbonGroup.addItem(createGridCopyAction());
        ribbonGroup.addItem(createGridDeleteAction());
        return ribbonGroup;
    }

    private RibbonActionItem createGridDeleteAction() {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(translateRibbonAction("#{grid}.performDelete;"));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonDeleteAction.setEnabled(false);
        // ribbonDeleteAction.setMessage("noRecordSelected");
        ribbonDeleteAction.setScript("var listener = {onChange: function(selectedArray) {if (selectedArray.length == 0) {"
                + "this.disable();} else {this.enable();}}}; #{grid}.addOnChangeListener(listener);");
        return ribbonDeleteAction;
    }

    private RibbonActionItem createGridCopyAction() {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(translateRibbonAction("#{grid}.performCopy;"));
        ribbonCopyAction.setIcon("copyIcon16.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setEnabled(false);
        // ribbonCopyAction.setMessage("noRecordSelected");
        ribbonCopyAction.setScript("var listener = {onChange: function(selectedArray) {if (selectedArray.length == 0) {"
                + "this.disable();} else {this.enable();}}}; #{grid}.addOnChangeListener(listener);");
        ribbonCopyAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCopyAction;
    }

    private RibbonActionItem createGridNewAction() {
        RibbonActionItem ribbonNewAction = new RibbonActionItem();
        ribbonNewAction.setAction(translateRibbonAction("#{grid}.performNew;"));
        ribbonNewAction.setIcon("newIcon24.png");
        ribbonNewAction.setName("new");
        ribbonNewAction.setEnabled(true);
        ribbonNewAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonNewAction;
    }

    private RibbonGroup createFormSaveCopyAndRemoveActionsTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction());
        ribbonGroup.addItem(createFormSaveAndBackAction());
        ribbonGroup.addItem(createFormSaveAndNewAction());
        ribbonGroup.addItem(createFormCopyAction());
        ribbonGroup.addItem(createFormCancelAction());
        ribbonGroup.addItem(createFormDeleteAction());
        return ribbonGroup;
    }

    private RibbonGroup createFormSaveAndRemoveActionsTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAction());
        ribbonGroup.addItem(createFormSaveAndBackAction());
        ribbonGroup.addItem(createFormCancelAction());
        ribbonGroup.addItem(createFormDeleteAction());
        return ribbonGroup;
    }

    private RibbonGroup createFormSaveAndBackAndRemoveActionsTemplate() {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(createFormSaveAndBackAction());
        ribbonGroup.addItem(createFormCancelAction());
        ribbonGroup.addItem(createFormDeleteAction());
        return ribbonGroup;
    }

    private RibbonGroup createFormSaveActionTemplate() {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(translateRibbonAction("#{form}.performSave; #{window}.performBack"));
        ribbonSaveAction.setIcon("saveBackIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        ribbonSaveAction.setEnabled(true);
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName("actions");
        ribbonGroup.addItem(ribbonSaveAction);

        return ribbonGroup;
    }

    private RibbonActionItem createFormDeleteAction() {
        RibbonActionItem ribbonDeleteAction = new RibbonActionItem();
        ribbonDeleteAction.setAction(translateRibbonAction("#{form}.performDelete; #{window}.performBack"));
        ribbonDeleteAction.setIcon("deleteIcon16.png");
        ribbonDeleteAction.setName("delete");
        ribbonDeleteAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonDeleteAction.setEnabled(false);
        // ribbonDeleteAction.setMessage("recordNotCreated");
        ribbonDeleteAction
                .setScript("var listener = {onSetValue: function(value) {if (!value || !value.content) return; if (value.content.entityId) {"
                        + "this.enable();} else {this.disable();}}}; #{form}.addOnChangeListener(listener);");
        return ribbonDeleteAction;
    }

    private RibbonActionItem createFormCancelAction() {
        RibbonActionItem ribbonCancelAction = new RibbonActionItem();
        ribbonCancelAction.setAction(translateRibbonAction("#{form}.performCancel;"));
        ribbonCancelAction.setIcon("cancelIcon16.png");
        ribbonCancelAction.setName("cancel");
        ribbonCancelAction.setEnabled(true);
        ribbonCancelAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonCancelAction;
    }

    private RibbonActionItem createFormCopyAction() {
        RibbonActionItem ribbonCopyAction = new RibbonActionItem();
        ribbonCopyAction.setAction(translateRibbonAction("#{form}.performCopy;"));
        ribbonCopyAction.setIcon("copyIcon16.png");
        ribbonCopyAction.setName("copy");
        ribbonCopyAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        ribbonCopyAction.setEnabled(false);
        // ribbonCopyAction.setMessage("recordNotCreated");
        ribbonCopyAction
                .setScript("var listener = {onSetValue: function(value) {if (!value || !value.content) return; if (value.content.entityId) {"
                        + "this.enable();} else {this.disable();}}}; #{form}.addOnChangeListener(listener);");
        return ribbonCopyAction;
    }

    private RibbonActionItem createFormSaveAndBackAction() {
        RibbonActionItem ribbonSaveBackAction = new RibbonActionItem();
        ribbonSaveBackAction.setAction(translateRibbonAction("#{form}.performSave; #{window}.performBack;"));
        ribbonSaveBackAction.setIcon("saveBackIcon24.png");
        ribbonSaveBackAction.setName("saveBack");
        ribbonSaveBackAction.setEnabled(true);
        ribbonSaveBackAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveBackAction;
    }

    private RibbonActionItem createFormSaveAndNewAction() {
        RibbonActionItem ribbonSaveNewAction = new RibbonActionItem();
        ribbonSaveNewAction.setAction(translateRibbonAction("#{form}.performSaveAndClear;"));
        ribbonSaveNewAction.setIcon("saveNewIcon16.png");
        ribbonSaveNewAction.setName("saveNew");
        ribbonSaveNewAction.setEnabled(true);
        ribbonSaveNewAction.setType(RibbonActionItem.Type.SMALL_BUTTON);
        return ribbonSaveNewAction;
    }

    private RibbonActionItem createFormSaveAction() {
        RibbonActionItem ribbonSaveAction = new RibbonActionItem();
        ribbonSaveAction.setAction(translateRibbonAction("#{form}.performSave;"));
        ribbonSaveAction.setIcon("saveIcon24.png");
        ribbonSaveAction.setName("save");
        ribbonSaveAction.setEnabled(true);
        ribbonSaveAction.setType(RibbonActionItem.Type.BIG_BUTTON);
        return ribbonSaveAction;
    }

}
