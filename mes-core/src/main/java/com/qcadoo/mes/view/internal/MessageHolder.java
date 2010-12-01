package com.qcadoo.mes.view.internal;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;

public class MessageHolder {

    private final List<Pair<String, MessageType>> messages = new ArrayList<Pair<String, MessageType>>();

    public void addMessage(final String message, final MessageType type) {
        messages.add(Pair.of(message, type));
    }

    public JSONArray renderMessages() throws JSONException {
        JSONArray json = new JSONArray();

        for (Pair<String, MessageType> message : messages) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put(ComponentState.JSON_MESSAGE_BODY, message.getKey());
            jsonMessage.put(ComponentState.JSON_MESSAGE_TYPE, message.getValue().ordinal());
            json.put(jsonMessage);
        }

        return json;
    }

}
