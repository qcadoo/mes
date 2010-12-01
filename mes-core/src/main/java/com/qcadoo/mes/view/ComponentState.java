package com.qcadoo.mes.view;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public interface ComponentState {

    String JSON_UPDATE_STATE = "updateState";

    String JSON_VISIBLE = "v";

    String JSON_ENABLED = "e";

    String JSON_CONTENT = "content";

    String JSON_CONTEXT = "context";

    String JSON_VALUE = "value";

    String JSON_CHILDREN = "components";

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

    boolean isEnabled();

    void setEnabled(boolean enable);

}
