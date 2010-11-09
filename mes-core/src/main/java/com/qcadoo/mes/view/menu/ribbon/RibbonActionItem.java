package com.qcadoo.mes.view.menu.ribbon;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents single ribbon item
 */
public class RibbonActionItem {

    /**
     * type of ribbon item
     */
    public static enum Type {
        /**
         * simple big button
         */
        BIG_BUTTON,
        /**
         * simple small button
         */
        SMALL_BUTTON,
        /**
         * checkbox
         */
        CHECKBOX,
        /**
         * combobox
         */
        COMBOBOX
    }

    private String name;

    private Type type;

    private String icon;

    private String clickAction;

    /**
     * get defined item click action
     * 
     * @return defined item click action
     */
    public final String getAction() {
        return clickAction;
    }

    /**
     * set defined item action
     * 
     * @param clickAction
     *            defined item action
     */
    public final void setAction(final String clickAction) {
        this.clickAction = clickAction;
    }

    /**
     * get identifier of this ribbon item
     * 
     * @return identifier of this ribbon item
     */
    public final String getName() {
        return name;
    }

    /**
     * set identifier of this ribbon item
     * 
     * @param name
     *            identifier of this ribbon item
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * get item type
     * 
     * @return item type
     */
    public final Type getType() {
        return type;
    }

    /**
     * set item type
     * 
     * @param type
     *            item type
     */
    public final void setType(final Type type) {
        this.type = type;
    }

    /**
     * get item icon (null if item without icon)
     * 
     * @return item icon
     */
    public final String getIcon() {
        return icon;
    }

    /**
     * set item icon (null if item without icon)
     * 
     * @param icon
     *            item icon
     */
    public final void setIcon(final String icon) {
        this.icon = icon;
    }

    /**
     * generates JSON representation of this ribbon item
     * 
     * @return JSON representation of this ribbon item
     * @throws JSONException
     */
    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", name);
        itemObject.put("type", type);
        itemObject.put("icon", icon);
        itemObject.put("clickAction", clickAction);
        return itemObject;
    }

}
