package com.qcadoo.mes.view;

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

    enum MessageType {
        FAILURE, SUCCESS, INFO
    }

    String getName();

    void initialize(JSONObject json, Locale locale) throws JSONException;

    void performEvent(ViewDefinitionState viewDefinitionState, String event, String... args);

    JSONObject render() throws JSONException;

    void setFieldValue(Object value);

    Object getFieldValue();

    void addMessage(String message, MessageType type);

    boolean isVisible();

    void setVisible(boolean visible);

    boolean isEnabled();

    void setEnabled(boolean enable);

    Locale getLocale();

    boolean isHasError();

}
