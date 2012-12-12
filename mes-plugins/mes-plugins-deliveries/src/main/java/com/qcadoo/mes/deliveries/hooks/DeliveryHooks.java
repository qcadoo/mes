/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DRAFT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private DeliveryStateChangeDescriber describer;

    public void setInitialState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, DRAFT);
    }

    public void clearFieldsOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(STATE, DeliveryStateStringValues.DRAFT);
        entity.setField(EXTERNAL_NUMBER, null);
        entity.setField(EXTERNAL_SYNCHRONIZED, true);
    }

}
