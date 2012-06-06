package com.qcadoo.mes.states.service;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.hasFailureMessages;

import java.util.List;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

public final class StateChangePhaseUtil {

    private StateChangePhaseUtil() {
    }

    public static boolean canRun(final StateChangeService stateChangeService, final Entity stateChangeEntity) {
        final StateChangeEntityDescriber describer = stateChangeService.getChangeEntityDescriber();
        boolean isFinished = stateChangeEntity.getBooleanField(describer.getFinishedFieldName());
        List<Entity> messages = stateChangeEntity.getHasManyField(describer.getMessagesFieldName());

        Preconditions.checkNotNull(messages, "entity " + stateChangeEntity + " should have messages has many field!");
        return !isFinished && !hasFailureMessages(messages);
    }
}
