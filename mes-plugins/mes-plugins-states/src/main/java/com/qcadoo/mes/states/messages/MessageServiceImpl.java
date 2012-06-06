package com.qcadoo.mes.states.messages;

import static com.qcadoo.mes.states.constants.StatesConstants.MODEL_MESSAGE;
import static com.qcadoo.mes.states.constants.StatesConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.mes.states.messages.util.MessagesUtil;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createMessage(final MessageType type, final String translationKey, final String... translationArgs) {
        Entity message = getDataDefinition().create();
        message.setField(MessageFields.TYPE, type.getStringValue());
        message.setField(MessageFields.TRANSLATION_KEY, translationKey);
        message.setField(MessageFields.TRANSLATION_ARGS, MessagesUtil.joinArgs(translationArgs));
        return message;
    }

    protected DataDefinition getDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_MESSAGE);
    }
}
