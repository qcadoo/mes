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
package com.qcadoo.mes.costNormsForOperationInOrder.hooks;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class HourlyCostNormsInOrderDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setLastUpdateDateTioc(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");
        Entity order = orderForm.getEntity().getDataDefinition().get(orderForm.getEntityId());
        List<Entity> tiocs = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .find().add(SearchRestrictions.belongsTo("order", order)).addOrder(SearchOrders.desc("updateDate")).list()
                .getEntities();
        if (tiocs.isEmpty()) {
            return;
        }
        FieldComponent lastUpdate = (FieldComponent) view.getComponentByReference("lastUpdate");
        String updateDate = DateFormat.getDateTimeInstance().format((Date) (tiocs.get(0).getField("updateDate")));
        lastUpdate.setFieldValue(updateDate);
        lastUpdate.requestComponentUpdateState();
    }
}
