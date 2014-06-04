/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.states.messages;

import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;

import java.util.List;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

public class MessagesHolderImpl implements MessagesHolder {

    private final MessageService messageService;

    private final List<Entity> messagesList;

    public MessagesHolderImpl(final MessageService messageService) {
        this.messageService = messageService;
        this.messagesList = Lists.newArrayList();
    }

    @Override
    public void addFieldMessage(final String translationKey, final StateMessageType type, final String fieldName,
            final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, type, true, fieldName, translationArgs));
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final String... translationArgs) {
        addMessage(translationKey, type, true, translationArgs);
    }

    @Override
    public void addMessage(final String translationKey, final StateMessageType type, final boolean autoClose,
            final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, type, autoClose, null, translationArgs));
    }

    @Override
    public void addFieldValidationError(final String translationKey, final String fieldName, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, VALIDATION_ERROR, true, fieldName, translationArgs));
    }

    @Override
    public void addValidationError(final String translationKey, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, VALIDATION_ERROR, true, null, translationArgs));
    }

    @Override
    public List<Entity> getAllMessages() {
        return messagesList;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public void addValidationError(final String translationKey, final boolean autoClose, final String... translationArgs) {
        messagesList.add(messageService.createMessage(translationKey, VALIDATION_ERROR, autoClose, null, translationArgs));

    }

}
