package com.qcadoo.mes.newview;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public interface ComponentState {

    String JSON_UPDATE_STATE = "updateState";

    String JSON_VISIBLE = "v";

    String JSON_ENABLE = "e";

    String JSON_CONTENT = "content";

    String JSON_VALUE = "value";

    String JSON_CHILDREN = "children";

    String JSON_MESSAGES = "messages";

    String JSON_MESSAGE_BODY = "m";

    String JSON_MESSAGE_TYPE = "t";

    enum MessageType {
        FAILURE, SUCCESS
    }

    String getName();

    void initialize(JSONObject json, Locale locale) throws JSONException;

    void performEvent(String event, String... args);

    void beforeRender();

    JSONObject render() throws JSONException;

    void setFieldValue(Object value);

    Object getFieldValue();

    void addMessage(String message, MessageType type);

    boolean isVisible();

    void setVisible(boolean visible);

    boolean isEnable();

    void setEnable(boolean enable);

}
