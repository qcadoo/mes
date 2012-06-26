package com.qcadoo.mes.states.service;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasFailureMessages;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasValidationErrorMessages;

import java.util.List;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

public final class StateChangePhaseUtil {

    private StateChangePhaseUtil() {
    }

    public static boolean canRun(final StateChangeContext stateChangeContext) {
        List<Entity> messages = stateChangeContext.getAllMessages();

        Preconditions.checkNotNull(messages, "entity " + stateChangeContext.getStateChangeEntity()
                + " should have messages has many field!");
        return stateChangeContext.isOwnerValid() && stateChangeContext.getStatus().canContinue() && !hasFailureMessages(messages)
                && !hasValidationErrorMessages(messages);
    }
}
