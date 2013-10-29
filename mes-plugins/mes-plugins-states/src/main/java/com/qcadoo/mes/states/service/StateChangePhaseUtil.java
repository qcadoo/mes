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
                + " should have messages in corresponding hasMany field!");
        return stateChangeContext.isOwnerValid() && stateChangeContext.getStatus().canContinue() && !hasFailureMessages(messages)
                && !hasValidationErrorMessages(messages);
    }
}
