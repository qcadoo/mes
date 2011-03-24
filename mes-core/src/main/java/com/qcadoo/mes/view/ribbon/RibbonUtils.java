package com.qcadoo.mes.view.ribbon;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;

public class RibbonUtils {

    private RibbonTemplates ribbonTemplates;

    private static RibbonUtils instance;

    private RibbonUtils() {
        ribbonTemplates = new RibbonTemplates();
    }

    public static RibbonUtils getInstance() {
        if (instance == null) {
            synchronized (RibbonUtils.class) {
                if (instance == null) {
                    instance = new RibbonUtils();
                }
            }
        }
        return instance;
    }

    public Ribbon parseRibbon(final Node ribbonNode, final ViewDefinitionParser parser, final ViewDefinition viewDefinition) {
        Ribbon ribbon = new Ribbon();

        NodeList childNodes = ribbonNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("group".equals(child.getNodeName())) {
                ribbon.addGroup(parseRibbonGroup(child, parser, viewDefinition));
            }
        }

        return ribbon;
    }

    public JSONObject translateRibbon(final Ribbon ribbon, final Locale locale, final AbstractComponentPattern pattern)
            throws JSONException {
        JSONObject json = ribbon.getAsJson();

        for (int i = 0; i < json.getJSONArray("groups").length(); i++) {
            JSONObject group = json.getJSONArray("groups").getJSONObject(i);
            group.put("label",
                    pattern.getTranslationService().translate(getTranslationCodes(group.getString("name"), pattern), locale));
            translateRibbonItems(group, group.getString("name") + ".", locale, pattern);
        }

        return json;
    }

    private void translateRibbonItems(final JSONObject owner, final String prefix, final Locale locale,
            final AbstractComponentPattern pattern) throws JSONException {
        if (owner.has("items")) {
            for (int j = 0; j < owner.getJSONArray("items").length(); j++) {
                JSONObject item = owner.getJSONArray("items").getJSONObject(j);

                String label = pattern.getTranslationService().translate(
                        getTranslationCodes(prefix + item.getString("name"), pattern), locale);
                item.put("label", label);

                if (item.has("script")) {
                    String script = item.getString("script");
                    if (script != null) {
                        item.put("script", pattern.prepareScript(script, locale));
                    }
                }

                if (item.has("message")) {
                    String message = item.getString("message");
                    if (message.contains(".")) {
                        message = pattern.getTranslationService().translate(message, locale);
                    } else {
                        message = pattern.getTranslationService().translate("core.message." + message, locale);
                    }
                    item.put("message", pattern.prepareScript(message, locale));
                }

                translateRibbonItems(item, prefix + item.getString("name") + ".", locale, pattern);
            }
        }
    }

    private List<String> getTranslationCodes(final String key, final AbstractComponentPattern pattern) {
        return Arrays.asList(new String[] { pattern.getTranslationPath() + ".ribbon." + key, "core.ribbon." + key });
    }

    public RibbonGroup parseRibbonGroup(final Node groupNode, final ViewDefinitionParser parser,
            final ViewDefinition viewDefinition) {
        String template = parser.getStringAttribute(groupNode, "template");

        if (template != null) {
            return ribbonTemplates.getGroupTemplate(template, viewDefinition);
        } else {
            RibbonGroup ribbonGroup = new RibbonGroup();
            ribbonGroup.setName(parser.getStringAttribute(groupNode, "name"));

            NodeList childNodes = groupNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    ribbonGroup.addItem(parseRibbonItem(child, parser, viewDefinition));
                }
            }

            return ribbonGroup;
        }
    }

    private RibbonActionItem parseRibbonItem(final Node itemNode, final ViewDefinitionParser parser,
            final ViewDefinition viewDefinition) {
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
        item.setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action"), viewDefinition));
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
                    ((RibbonComboItem) item).addItem(parseRibbonItem(child, parser, viewDefinition));
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
            (item).setAction(translateRibbonAction(parser.getStringAttribute(itemNode, "action"), viewDefinition));
        }

        return item;
    }

    public String translateRibbonAction(final String action, final ViewDefinition viewDefinition) {
        if (action == null) {
            return null;
        }

        Pattern p = Pattern.compile("#\\{([^\\}]+)\\}");
        Matcher m = p.matcher(action);

        String translateAction = action;

        while (m.find()) {
            ComponentPattern actionComponentPattern = viewDefinition.getComponentByReference(m.group(1));

            if (actionComponentPattern == null) {
                throw new IllegalStateException("Cannot find action component for: " + action + " [" + m.group(1) + "]");
            }

            translateAction = translateAction.replace("#{" + m.group(1) + "}", "#{" + actionComponentPattern.getPath() + "}");
        }

        return translateAction;
    }
}
