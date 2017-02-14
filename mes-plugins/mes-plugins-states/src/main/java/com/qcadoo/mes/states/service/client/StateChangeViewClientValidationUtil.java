/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.MessagesHolder;
import com.qcadoo.mes.states.messages.util.MessagesUtil;
import com.qcadoo.mes.states.messages.util.ValidationMessagePredicate;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.components.FormComponent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.mes.states.messages.constants.MessageFields.CORRESPOND_FIELD_NAME;
import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.*;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Service
public class StateChangeViewClientValidationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStateChangeViewClient.class);

    @Autowired
    private TranslationService translationService;

    private static final Predicate VALIDATION_MESSAGES_PREDICATE = new ValidationMessagePredicate();

    public void addValidationErrorMessages(final ComponentState component, final StateChangeContext stateChangeContext) {
        addValidationErrorMessages(component, stateChangeContext.getOwner(), stateChangeContext);
    }

    public void addValidationErrorMessages(final ComponentState component, final Entity entity,
            final MessagesHolder messagesHolder) {
        if (component instanceof FormComponent) {
            addValidationErrorsToForm((FormComponent) component, messagesHolder.getAllMessages());
        } else {
            addValidationErrors(component, entity, messagesHolder.getAllMessages());
        }
    }

    private void addValidationErrorsToForm(final FormComponent form, final List<Entity> messagesList) {
        final Entity entity = form.getEntity();
        final List<Entity> messages = Lists.newArrayList(messagesList);
        CollectionUtils.filter(messages, VALIDATION_MESSAGES_PREDICATE);
        for (Entity message : messages) {
            assignMessageToEntity(entity, message);
        }
        if (!entity.isValid()) {
            form.addMessage("qcadooView.message.saveFailedMessage", MessageType.FAILURE);
        }
        form.setEntity(entity);
    }

    private void assignMessageToEntity(final Entity entity, final Entity message) {
        DataDefinition dataDefinition = entity.getDataDefinition();
        String correspondFieldName = MessagesUtil.getCorrespondFieldName(message);
        if (StringUtils.isNotBlank(correspondFieldName) && dataDefinition.getFields().containsKey(correspondFieldName)) {
            LOG.info(String.format("Change state error. Entity name : %S id : %d. Field : %S %S", entity.getDataDefinition()
                    .getName(), entity.getId(), correspondFieldName, translationService.translate(getKey(message),
                    LocaleContextHolder.getLocale(), getArgs(message))));
            entity.addError(entity.getDataDefinition().getField(correspondFieldName), getKey(message), getArgs(message));
        } else {
            LOG.info(String.format("Change state error. Entity name : %S id : %d. %S", entity.getDataDefinition().getName(),
                    entity.getId(),
                    translationService.translate(getKey(message), LocaleContextHolder.getLocale(), getArgs(message))));

            entity.addGlobalError(getKey(message), isAutoClosed(message), getArgs(message));
        }
    }

    private void addValidationErrors(final ComponentState component, final Entity entity, final List<Entity> messages) {
        final List<String> errorMessages = Lists.newArrayList();

        CollectionUtils.filter(messages, VALIDATION_MESSAGES_PREDICATE);
        for (Entity message : messages) {
            if (MessagesUtil.hasCorrespondField(message)) {
                String msg = composeTranslatedFieldValidationMessage(entity, message);
                errorMessages.add(msg);
                LOG.info(String.format("Change state error. Entity name : %S id : %d. %S", entity.getDataDefinition().getName(),
                        entity.getId(), msg));
            } else {
                String msg = composeTranslatedGlobalValidationMessage(message);
                errorMessages.add(msg);
                LOG.info(String.format("Change state error. Entity name : %S id : %d. %S", entity.getDataDefinition().getName(),
                        entity.getId(), msg));
            }
        }

        if (!errorMessages.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(translationService.translate("states.messages.change.failure.validationErrors", getLocale(),
                    join(errorMessages, ' ')));
            component.addTranslatedMessage(sb.toString(), convertViewMessageType(VALIDATION_ERROR));
        }
    }

    private String composeTranslatedGlobalValidationMessage(final Entity globalMessage) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<li>");
        sb.append(translationService.translate(getKey(globalMessage), getLocale(), getArgs(globalMessage)));
        sb.append("</li>");
        return sb.toString();
    }

    private String composeTranslatedFieldValidationMessage(final Entity entity, final Entity fieldMessage) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<li>");
        sb.append(getTranslatedFieldName(entity, fieldMessage.getStringField(CORRESPOND_FIELD_NAME)));
        sb.append(": ");
        sb.append(translationService.translate(getKey(fieldMessage), getLocale(), getArgs(fieldMessage)));
        sb.append("</li>");
        return sb.toString();
    }

    private String getTranslatedFieldName(final Entity entity, final String fieldName) {
        final StringBuilder sb = new StringBuilder();
        sb.append(entity.getDataDefinition().getPluginIdentifier());
        sb.append('.');
        sb.append(entity.getDataDefinition().getName());
        sb.append('.');
        sb.append(fieldName);
        sb.append(".label");
        return translationService.translate(sb.toString(), getLocale());
    }
}
