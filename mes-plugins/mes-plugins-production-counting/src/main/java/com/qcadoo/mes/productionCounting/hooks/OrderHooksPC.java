/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooksPC {

    private static final List<String> L_ORDER_FIELD_NAMES = Lists.newArrayList(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
            OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.REGISTER_PRODUCTION_TIME);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderService orderService;

    public void onSave(final DataDefinition orderDD, final Entity order) {
        if (Objects.nonNull(order.getId())) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            Entity orderFromDB = orderService.getOrder(order.getId());
            Entity technologyDB = orderFromDB.getBelongsToField(OrderFields.TECHNOLOGY);

            if (Objects.nonNull(technology) && Objects.nonNull(technologyDB)
                    && !technology.getId().equals(technologyDB.getId())) {
                setOrderWithTechnologyProductionCountingValues(orderDD, order, technology);
            }
        }
    }

    public void onCreate(final DataDefinition orderDD, final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            setOrderWithTechnologyProductionCountingValues(orderDD, order, technology);
        } else {
            setOrderWithDefaultProductionCountingValues(orderDD, order);
        }
    }

    private void setOrderWithTechnologyProductionCountingValues(final DataDefinition orderDD, final Entity order,
                                                                final Entity technology) {
        order.setField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                technology.getField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        order.setField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
                technology.getField(TechnologyFieldsPC.REGISTER_QUANTITY_IN_PRODUCT));
        order.setField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT,
                technology.getField(TechnologyFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT));
        order.setField(OrderFieldsPC.REGISTER_PRODUCTION_TIME, technology.getField(TechnologyFieldsPC.REGISTER_PRODUCTION_TIME));
    }

    private void setOrderWithDefaultProductionCountingValues(final DataDefinition orderDD, final Entity order) {
        for (String fieldName : L_ORDER_FIELD_NAMES) {
            if (OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING.equals(fieldName)) {
                if (StringUtils.isEmpty(order.getStringField(fieldName))) {
                    order.setField(fieldName, parameterService.getParameter().getField(fieldName));
                }
            } else {
                if (Objects.isNull(order.getField(fieldName))) {
                    order.setField(fieldName, parameterService.getParameter().getField(fieldName));
                }
            }
        }
    }

    public boolean validatesWith(final DataDefinition parameterDD, final Entity order) {
        if (Objects.isNull(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            order.addError(parameterDD.getField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING),
                    "qcadooView.validate.field.error.missing");
            order.addGlobalError("orders.order.typeOfProductionRecording.error.empty");

            return false;
        }
        if (!TypeOfProductionRecording.FOR_EACH.getStringValue()
                .equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                && !order.getHasManyField(OrderFields.OPERATIONAL_TASKS).isEmpty()) {
            order.addError(parameterDD.getField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING),
                    "orders.order.typeOfProductionRecording.error.hasOperationalTasks");
            order.addGlobalError("orders.order.typeOfProductionRecording.error.hasOperationalTasks");

            return false;
        }

        return true;
    }

}
