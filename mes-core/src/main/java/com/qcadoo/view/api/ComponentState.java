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

package com.qcadoo.view.api;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public interface ComponentState {

    String JSON_UPDATE_STATE = "updateState";

    String JSON_VISIBLE = "visible";

    String JSON_ENABLED = "enabled";

    String JSON_CONTENT = "content";

    String JSON_CONTEXT = "context";

    String JSON_VALUE = "value";

    String JSON_CHILDREN = "components";

    String JSON_MESSAGES = "messages";

    String JSON_MESSAGE_TITLE = "title";

    String JSON_MESSAGE_BODY = "content";

    String JSON_MESSAGE_TYPE = "type";

    String JSON_MESSAGE_AUTOCLOSE = "autoClose";

    public enum MessageType {
        FAILURE, SUCCESS, INFO
    }

    String getName();

    void initialize(JSONObject json, Locale locale) throws JSONException;

    void performEvent(ViewDefinitionState viewDefinitionState, String event, String... args);

    JSONObject render() throws JSONException;

    void setFieldValue(Object value);

    Object getFieldValue();

    void addMessage(String message, MessageType type);

    void addMessage(String message, MessageType type, boolean autoClose);

    boolean isVisible();

    void setVisible(boolean visible);

    boolean isEnabled();

    void setEnabled(boolean enable);

    Locale getLocale();

    boolean isHasError();

}
