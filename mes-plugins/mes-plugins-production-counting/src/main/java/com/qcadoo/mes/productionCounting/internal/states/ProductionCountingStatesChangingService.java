/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingStatesChangingService {

    private final List<RecordStateListener> stateListeners = new LinkedList<RecordStateListener>();

    private static final String FIELD_STATE = "state";

    public void addRecordStateListener(final RecordStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeRecordStateListener(final RecordStateListener listener) {
        stateListeners.remove(listener);
    }

    void performChangeState(final Entity newEntity, final Entity oldEntity) {
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
            throw new IllegalStateException("State value is illegal");
        }

        if (oldEntity == null && !state.equals(DRAFT)) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && state.equals(ACCEPTED) && !DRAFT.getStringValue().equals(oldEntity.getStringField(FIELD_STATE))) {
            throw new IllegalStateException();
        }
        switch (state) {
            case ACCEPTED:
                performAccepted(newEntity, oldEntity);
                break;
            case DECLINED:
                performDeclined(newEntity, oldEntity);
                break;
            case DRAFT:
                break;
            default:
                throw new IllegalStateException("Record entity has invalid state value");
        }

    }

    private void performDeclined(final Entity productionRecord, final Entity prevState) {
        for (RecordStateListener listener : stateListeners) {
            listener.onDeclined(productionRecord, prevState);
        }
    }

    private void performAccepted(final Entity productionRecord, final Entity prevState) {
        for (RecordStateListener listener : stateListeners) {
            listener.onAccepted(productionRecord, prevState);
        }
    }

}
