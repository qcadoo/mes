package com.qcadoo.mes.view.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;

public final class MessageHolder {

    private final TranslationService translationService;

    private final Locale locale;

    private final List<Object[]> messages = new ArrayList<Object[]>();

    public MessageHolder(final TranslationService translationService, final Locale locale) {
        this.translationService = translationService;
        this.locale = locale;
    }

    public void addMessage(final String title, final String message, final MessageType type) {
        messages.add(new Object[] { getTranslatedTitle(title, type), message, type });
    }

    private Object getTranslatedTitle(final String title, final MessageType type) {
        if (title != null) {
            return title;
        } else {
            return translationService.translate("commons.notification." + type.toString().toLowerCase(), locale);
        }
    }

    public JSONArray renderMessages() throws JSONException {
        JSONArray json = new JSONArray();

        for (Object[] message : messages) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put(ComponentState.JSON_MESSAGE_TITLE, message[0]);
            jsonMessage.put(ComponentState.JSON_MESSAGE_BODY, message[1]);
            jsonMessage.put(ComponentState.JSON_MESSAGE_TYPE, message[2]);
            json.put(jsonMessage);
        }

        return json;
    }

}
