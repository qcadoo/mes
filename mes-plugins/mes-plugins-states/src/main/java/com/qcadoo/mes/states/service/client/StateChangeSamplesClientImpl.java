package com.qcadoo.mes.states.service.client;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.getArgs;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getCorrespondFieldName;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getKey;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.exception.StateChangeException;
import com.qcadoo.mes.states.messages.util.MessagesUtil;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.StateChangeServiceResolver;
import com.qcadoo.model.api.Entity;

@Service
public class StateChangeSamplesClientImpl implements StateChangeSamplesClient {

    @Autowired
    private StateChangeServiceResolver stateChangeServiceResolver;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private TranslationService translationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StateChangeSamplesClient.class);

    @Override
    public Entity changeState(final Entity entity, final String targetState) {
        Entity resultEntity = entity;
        final StateChangeService stateChangeService = stateChangeServiceResolver.get(entity.getDataDefinition());
        if (stateChangeService != null) {
            resultEntity = performChange(stateChangeService, resultEntity, targetState);
        } else {
            resultEntity = performDummyChange(resultEntity, targetState);
        }
        return resultEntity;
    }

    private Entity performChange(final StateChangeService stateChangeService, final Entity entity, final String targetState) {
        final StateChangeEntityDescriber describer = stateChangeService.getChangeEntityDescriber();
        final StateChangeContext stateChangeContext = stateChangeContextBuilder.build(describer, entity, targetState);
        stateChangeService.changeState(stateChangeContext);
        checkResults(stateChangeContext);

        Entity resultEntity = entity;
        if (entity.getId() == null) {
            resultEntity = entity.getDataDefinition().save(entity);
        } else {
            resultEntity = entity.getDataDefinition().get(entity.getId());
        }

        return resultEntity;
    }

    private Entity performDummyChange(final Entity entity, final String targetState) {
        entity.setField("state", targetState);
        return entity.getDataDefinition().save(entity);
    }

    private void checkResults(final StateChangeContext stateChangeContext) {
        checkValidationErrors(stateChangeContext);
        if (StateChangeStatus.FAILURE.equals(stateChangeContext.getStatus())) {
            throw new StateChangeException("State change failed");
        }
    }

    private void checkValidationErrors(final StateChangeContext stateChangeContext) {
        final List<Entity> messages = stateChangeContext.getAllMessages();
        if (!MessagesUtil.hasValidationErrorMessages(messages)) {
            return;
        }
        if (LOGGER.isErrorEnabled()) {
            logValidationMessages(messages);
        }
        throw new StateChangeException("Entity has validation errors.");
    }

    private void logValidationMessages(final List<Entity> messages) {
        final StringBuilder logMessage = new StringBuilder();
        logMessage.append("State change failed due to validation errors:\n");
        for (Entity message : messages) {
            logMessage.append("\t- ");
            final String correspondingFieldName = getCorrespondFieldName(message);
            if (!StringUtils.isBlank(correspondingFieldName)) {
                logMessage.append(correspondingFieldName);
                logMessage.append(": ");
            }
            logMessage.append(translationService.translate(getKey(message), getLocale(), getArgs(message)));
            logMessage.append("\n");
        }
        LOGGER.error(logMessage.toString());
    }

}
