/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.productionCounting.internal.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.ACCEPTED;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.DECLINED;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.DRAFT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingStatesChangingService {

    private final Set<RecordStateListener> stateListeners = Sets.newHashSet();

    private static final String FIELD_STATE = "state";

    public void addRecordStateListener(final RecordStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeRecordStateListener(final RecordStateListener listener) {
        stateListeners.remove(listener);
    }

    List<ChangeRecordStateMessage> performChangeState(final Entity newEntity, final Entity oldEntity) {
        checkArgument(newEntity != null, "New entity is null");
        checkArgument(oldEntity != null, "Old entity is null");

        final ProductionCountingStates state;

        if (ACCEPTED.getStringValue().equals(newEntity.getStringField(FIELD_STATE))) {
            state = ProductionCountingStates.ACCEPTED;
        } else if (DRAFT.getStringValue().equals(newEntity.getStringField(FIELD_STATE))) {
            state = ProductionCountingStates.DRAFT;
        } else if (DECLINED.getStringValue().equals(newEntity.getStringField(FIELD_STATE))) {
            state = ProductionCountingStates.DECLINED;
        } else {
            return new ArrayList<ChangeRecordStateMessage>();
        }

        if (oldEntity == null && !state.equals(DRAFT)) {
            throw new IllegalStateException("Entity state is unsuitable.");
        }
        if (oldEntity != null
                && (state.equals(ACCEPTED) && DECLINED.getStringValue().equals(oldEntity.getStringField(FIELD_STATE)))
                && (state.equals(DRAFT) && ACCEPTED.getStringValue().equals(oldEntity.getStringField(FIELD_STATE)))) {
            throw new IllegalStateException("Entity state is unsuitable. State exists entity is "
                    + oldEntity.getStringField(FIELD_STATE) + " new state is " + newEntity.getStringField(FIELD_STATE));

        }
        switch (state) {
            case ACCEPTED:
                return performAccepted(newEntity, oldEntity);
            case DECLINED:
                return performDeclined(newEntity, oldEntity);
            case DRAFT:
                return new ArrayList<ChangeRecordStateMessage>();
            default:
                return Arrays.asList(ChangeRecordStateMessage.error(""));
        }

    }

    private List<ChangeRecordStateMessage> performDeclined(final Entity productionRecord, final Entity prevState) {
        List<ChangeRecordStateMessage> messages = new ArrayList<ChangeRecordStateMessage>();
        for (RecordStateListener listener : stateListeners) {
            messages = listener.onDeclined(productionRecord, prevState);
        }

        return messages;
    }

    private List<ChangeRecordStateMessage> performAccepted(final Entity productionRecord, final Entity prevState) {
        List<ChangeRecordStateMessage> messages = new ArrayList<ChangeRecordStateMessage>();
        for (RecordStateListener listener : stateListeners) {
            messages = listener.onAccepted(productionRecord, prevState);
        }

        return messages;
    }

}
