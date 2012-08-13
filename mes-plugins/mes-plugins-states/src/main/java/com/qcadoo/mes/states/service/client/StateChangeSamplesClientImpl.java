/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
        Entity resultEntity = null;
        final StateChangeService stateChangeService = stateChangeServiceResolver.get(entity.getDataDefinition());
        if (stateChangeService == null) {
            resultEntity = performDummyChange(entity, targetState);
        } else {
            resultEntity = performChange(stateChangeService, entity, targetState);
        }
        return resultEntity;
    }

    private Entity performChange(final StateChangeService stateChangeService, final Entity entity, final String targetState) {
        final StateChangeEntityDescriber describer = stateChangeService.getChangeEntityDescriber();
        final StateChangeContext stateChangeContext = stateChangeContextBuilder.build(describer, entity, targetState);
        stateChangeService.changeState(stateChangeContext);
        checkResults(stateChangeContext);

        Entity resultEntity = null;
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
