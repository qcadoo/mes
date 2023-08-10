/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.basicProductionCounting;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BpcOrderStateListenerService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void onAccept(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        final Entity technologyDb = order.getDataDefinition().get(order.getId()).getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology.getId()) && Objects.nonNull(technologyDb.getId())
                && !technology.getId().equals(technologyDb.getId())) {
            for (Entity pcq : order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)) {
                pcq.getDataDefinition().delete(pcq.getId());
            }
            for (Entity bpc : order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS)) {
                bpc.getDataDefinition().delete(bpc.getId());
            }
            for (Entity pqor : order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_OPERATION_RUNS)) {
                pqor.getDataDefinition().delete(pqor.getId());
            }
            order.setField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS, Lists.newArrayList());
            order.setField(OrderFieldsBPC.PRODUCTION_COUNTING_OPERATION_RUNS, Lists.newArrayList());
            order.setField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES, Lists.newArrayList());
            stateChangeContext.setOwner(order);
        }
    }

}
