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
package com.qcadoo.mes.masterOrders.hooks;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.COMPANY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEADLINE;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE;

@Service
public class MasterOrderHooks {

    @Autowired
    private ParameterService parameterService;

    public void onCreate(final DataDefinition dataDefinition, final Entity masterOrder) {
        setExternalSynchronizedField(masterOrder);
        setInitialState(masterOrder);
    }

    public void onSave(final DataDefinition dataDefinition, final Entity masterOrder) {

    }

    public void onUpdate(final DataDefinition dataDefinition, final Entity masterOrder) {
        changedDeadlineAndInOrder(masterOrder);
    }

    public void onCopy(final DataDefinition dataDefinition, final Entity masterOrder) {
        clearExternalFields(masterOrder);
        setInitialState(masterOrder);
    }

    private void setInitialState(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.NEW.getStringValue());
    }

    protected void setExternalSynchronizedField(final Entity masterOrder) {
        if (masterOrder.getField(MasterOrderFields.EXTERNAL_SYNCHRONIZED) == null) {
            masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
        }
    }

    protected void changedDeadlineAndInOrder(final Entity masterOrder) {
        Preconditions.checkArgument(masterOrder.getId() != null, "Method expects already persisted entity");
        Date deadline = masterOrder.getDateField(DEADLINE);
        Entity customer = masterOrder.getBelongsToField(COMPANY);
        Entity parameter = parameterService.getParameter();
        boolean deadlineForOrderBasedOnDeliveryDate = parameter.getBooleanField(DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE);

        if (deadline == null && customer == null) {
            return;
        }

        List<Entity> allOrders = masterOrder.getHasManyField(MasterOrderFields.ORDERS);
        boolean hasBeenChanged = false;

        for (Entity order : allOrders) {
            if (OrderState.of(order) != OrderState.PENDING) {
                continue;
            }

            if (!ObjectUtils.equals(order.getBelongsToField(OrderFields.COMPANY), customer)) {
                order.setField(OrderFields.COMPANY, customer);
                hasBeenChanged = true;
            }

            if (!deadlineForOrderBasedOnDeliveryDate && !ObjectUtils.equals(order.getDateField(OrderFields.DEADLINE), deadline)) {
                order.setField(OrderFields.DEADLINE, deadline);
                hasBeenChanged = true;
            }
        }

        if (hasBeenChanged) {
            masterOrder.setField(MasterOrderFields.ORDERS, allOrders);
        }
    }

    private void clearExternalFields(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_NUMBER, null);
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

}
