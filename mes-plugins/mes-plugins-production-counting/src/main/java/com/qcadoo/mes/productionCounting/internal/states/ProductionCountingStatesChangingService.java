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
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.DRAFT;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingStatesChangingService {

    void performChangeState(final Entity newEntity, final Entity oldEntity) {
        checkArgument(newEntity != null, "New entity is null");
        checkArgument(oldEntity != null, "Old entity is null");

        ProductionCountingStates state = ProductionCountingStates.valueOf(newEntity.getStringField("state"));

        if (oldEntity == null && !state.equals(DRAFT)) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && state.equals(ACCEPTED)
                && !ProductionCountingStates.valueOf(oldEntity.getStringField("state")).equals(DRAFT)) {
            throw new IllegalStateException();
        }
        switch (state) {
            case DRAFT:
                performAccepted();
                break;
            case ACCEPTED:
                performDeclined();
                break;
            default:
                throw new IllegalStateException("unknown product type");
        }

    }

    public void performAccepted() {

    }

    public void performDeclined() {
    }
}
