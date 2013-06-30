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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class HourlyCostNormsInOrderDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setLastUpdateDateTioc(view);
    }

    public void setLastUpdateDateTioc(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");
        Entity order = orderForm.getEntity().getDataDefinition().get(orderForm.getEntityId());
        List<Entity> tocs = order.getBelongsToField(OrderFields.TECHNOLOGY)
                .getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        if (tocs.isEmpty()) {
            return;
        }
        Date updateDate = new Date(0);
        for (Entity toc : tocs) {

            if (toc.getDateField("updateDate").after(updateDate)) {
                updateDate = toc.getDateField("updateDate");
            }

        }
        FieldComponent lastUpdate = (FieldComponent) view.getComponentByReference("lastUpdate");
        String lastUpdateDate = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, view.getLocale()).format(updateDate);
        lastUpdate.setFieldValue(lastUpdateDate);
        lastUpdate.requestComponentUpdateState();
    }
}
