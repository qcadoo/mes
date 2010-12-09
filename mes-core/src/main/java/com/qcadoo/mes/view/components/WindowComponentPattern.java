package com.qcadoo.mes.view.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
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

    private RibbonActionItem parseRibbonItem(final Node itemNode, final ViewDefinitionParser parser) {
        String stringType = itemNode.getNodeName();

        boolean combo = ("bigButtons".equals(stringType) || "smallButtons".equals(stringType));

        RibbonActionItem.Type type = null;
        if ("bigButtons".equals(stringType) || "bigButton".equals(stringType)) {
            type = RibbonActionItem.Type.BIG_BUTTON;
        } else if ("smallButtons".equals(stringType) || "smallButton".equals(stringType)) {
            type = RibbonActionItem.Type.SMALL_BUTTON;
        }

        RibbonActionItem item = null;
        if (combo) {
            item = new RibbonComboItem();
        } else {
            item = new RibbonActionItem();
        }

        item.setIcon(parser.getStringAttribute(itemNode, "icon"));
        item.setName(parser.getStringAttribute(itemNode, "name"));
        item.setAction(parser.getStringAttribute(itemNode, "action"));
        item.setType(type);

        if (combo) {
            NodeList childNodes = itemNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    ((RibbonComboItem) item).addItem(parseRibbonItem(child, parser));
                }
            }
        } else {
            (item).setAction(parser.getStringAttribute(itemNode, "action"));
        }

        return item;
    }

}
