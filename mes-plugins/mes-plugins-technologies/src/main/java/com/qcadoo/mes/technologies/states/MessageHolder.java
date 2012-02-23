/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.technologies.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public final class MessageHolder {

    private final String targetReferenceName;

    private final String messageKey;

    private final MessageType messageType;

    private final String[] vars;

    private MessageHolder(final String messageKey, final String targetReferenceName, final MessageType messageType,
            final String... vars) {
        this.messageKey = messageKey;
        this.targetReferenceName = targetReferenceName;
        this.messageType = messageType;
        this.vars = vars;
    }

    public static MessageHolder error(final String messageKey, final String targetReferenceName, final String... vars) {
        return new MessageHolder(messageKey, targetReferenceName, MessageType.FAILURE, vars);
    }

    public static MessageHolder error(final String messageKey, final String... vars) {
        return new MessageHolder(messageKey, null, MessageType.FAILURE, vars);
    }

    public static MessageHolder info(final String messageKey, final String... vars) {
        return new MessageHolder(messageKey, null, MessageType.INFO, vars);
    }

    public static MessageHolder success(final String messageKey, final String... vars) {
        return new MessageHolder(messageKey, null, MessageType.SUCCESS, vars);
    }

    public String getTargetReferenceName() {
        return targetReferenceName;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String[] getVars() {
        return vars;
    }

}
