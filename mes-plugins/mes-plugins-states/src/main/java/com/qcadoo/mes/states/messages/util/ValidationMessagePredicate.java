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
package com.qcadoo.mes.states.messages.util;

import static com.qcadoo.mes.states.messages.constants.StateMessageType.VALIDATION_ERROR;

import org.apache.commons.collections.Predicate;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.model.api.Entity;

public class ValidationMessagePredicate implements Predicate {

    @Override
    public boolean evaluate(final Object object) {
        return fieldIsEqual(object, MessageFields.TYPE, VALIDATION_ERROR.getStringValue());
    }

    protected final boolean fieldIsEqual(final Object messageObject, final String fieldName, final Object expectedValue) {
        if (!(messageObject instanceof Entity)) {
            return false;
        }
        final Entity messageEntity = (Entity) messageObject;
        final Object fieldValue = messageEntity.getField(fieldName);
        if (expectedValue == null && fieldValue == null) {
            return true;
        }
        if (expectedValue == null) {
            return false;
        }
        return expectedValue.equals(fieldValue);
    }

}
