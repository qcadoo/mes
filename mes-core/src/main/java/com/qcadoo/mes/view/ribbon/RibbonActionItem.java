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

package com.qcadoo.mes.view.ribbon;

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
        COMBOBOX,
        /**
         * small empty space
         */
        SMALL_EMPTY_SPACE
    }

    private String name;

    private Type type;

    private String icon;

    private String script;

    private String clickAction;

    private Boolean enabled;

    private String message;

    private boolean shouldBeUpdated = false;

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
        itemObject.put("enabled", enabled);
        itemObject.put("message", message);
        if (script != null) {
            itemObject.put("script", script);
        }
        itemObject.put("clickAction", clickAction);
        return itemObject;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RibbonActionItem getCopy() {
        RibbonActionItem copy = new RibbonActionItem();
        copyFields(copy);
        return copy;
    }

    protected void copyFields(RibbonActionItem item) {
        item.setName(name);
        item.setType(type);
        item.setIcon(icon);
        item.setScript(script);
        item.setAction(clickAction);
        item.setEnabled(enabled);
        item.setMessage(message);
    }

    public boolean isShouldBeUpdated() {
        return shouldBeUpdated;
    }

    public void setShouldBeUpdated(boolean shouldBeUpdated) {
        this.shouldBeUpdated = shouldBeUpdated;
    }
}
