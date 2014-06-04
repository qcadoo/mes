/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.states.messages.constants.MessageFields.CORRESPOND_FIELD_NAME;
import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.convertViewMessageType;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getArgs;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getKey;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.isAutoClosed;
import static org.apache.commons.lang.StringUtils.join;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class StateChangeViewClientValidationUtil {

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
            entity.addError(entity.getDataDefinition().getField(correspondFieldName), getKey(message), getArgs(message));
        } else {
            entity.addGlobalError(getKey(message), isAutoClosed(message), getArgs(message));
        }
    }

    private void addValidationErrors(final ComponentState component, final Entity entity, final List<Entity> messages) {
        final List<String> errorMessages = Lists.newArrayList();

        CollectionUtils.filter(messages, VALIDATION_MESSAGES_PREDICATE);
        for (Entity message : messages) {
            if (MessagesUtil.hasCorrespondField(message)) {
                errorMessages.add(composeTranslatedFieldValidationMessage(entity, message));
            } else {
                errorMessages.add(composeTranslatedGlobalValidationMessage(message));
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
