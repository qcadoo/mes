/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.10
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
package com.qcadoo.mes.orders.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class ChangeOrderStateMessage {

    private String referenceToField;

    private String message;

    private MessageType type;

    public ChangeOrderStateMessage(String message, String referenceToField, MessageType type) {
        this.referenceToField = referenceToField;
        this.message = message;
        this.type = type;
    }

    public static ChangeOrderStateMessage error(String message, String referenceToField) {
        return new ChangeOrderStateMessage(message, referenceToField, MessageType.FAILURE);
    }

    public static ChangeOrderStateMessage error(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.FAILURE);
    }

    public static ChangeOrderStateMessage info(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.INFO);
    }

    public static ChangeOrderStateMessage success(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.INFO);
    }

    public String getMessage() {
        return message;
    }

    public String getReferenceToField() {
        return referenceToField;
    }

    public void setReferenceToField(String referenceToField) {
        this.referenceToField = referenceToField;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

}
