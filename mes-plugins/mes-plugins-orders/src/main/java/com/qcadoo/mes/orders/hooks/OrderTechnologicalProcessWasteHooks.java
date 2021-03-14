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
package com.qcadoo.mes.orders.hooks;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessWasteFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderTechnologicalProcessWasteHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition orderTechnologicalProcessWasteDD, final Entity orderTechnologicalProcessWaste) {
        if (checkIfShouldInsertNumber(orderTechnologicalProcessWaste)) {
            orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.NUMBER, setNumberFromSequence());
        }

        Entity orderTechnologicalProcess = orderTechnologicalProcessWaste
                .getBelongsToField(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

            if (Objects.nonNull(order)) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.ORDER, order);
                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.PRODUCT, product);
            }
            if (Objects.nonNull(technologicalProcess)) {
                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.TECHNOLOGICAL_PROCESS,
                        technologicalProcess);
            }
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity orderTechnologicalProcessWaste) {
        if (Objects.nonNull(orderTechnologicalProcessWaste.getId())) {
            return false;
        }

        return StringUtils.isEmpty(orderTechnologicalProcessWaste.getStringField(OrderTechnologicalProcessWasteFields.NUMBER));
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("SELECT generate_ordertechnologicalprocesswaste_number()", Maps.newHashMap(),
                String.class);
    }

}
