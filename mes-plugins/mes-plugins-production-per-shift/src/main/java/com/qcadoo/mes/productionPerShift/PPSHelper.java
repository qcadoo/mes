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
package com.qcadoo.mes.productionPerShift;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class PPSHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getPpsIdForOrder(final Long orderId) {
        DataDefinition ppsDateDef = getPpsDataDef();
        String query = "select id as ppsId from #productionPerShift_productionPerShift where order.id = :orderId";
        Entity projectionResults = ppsDateDef.find(query).setLong("orderId", orderId).setMaxResults(1).uniqueResult();
        if (projectionResults == null) {
            return null;
        }
        return (Long) projectionResults.getField("ppsId");
    }

    public Long createPpsForOrderAndReturnId(final Long orderId) {
        DataDefinition ppsDataDef = getPpsDataDef();
        Entity pps = ppsDataDef.create();
        pps.setField("order", orderId);
        return ppsDataDef.save(pps).getId();
    }

    public DataDefinition getPpsDataDef() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
    }

    public DataDefinition getDailyProgressDataDef() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }

    public Entity getTiocFromOperationLookup(final ViewDefinitionState viewState) {
        ComponentState operationLookup = viewState.getComponentByReference("productionPerShiftOperation");
        Long id = (Long) operationLookup.getFieldValue();
        Entity tioc = null;
        if (id != null) {
            tioc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(id);
        }
        return tioc;
    }

    public boolean shouldHasCorrections(final ViewDefinitionState viewState) {
        return ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue().equals(
                PlannedProgressType.CORRECTED.getStringValue());
    }

    public Date getDateAfterStartOrderForProgress(final Entity order, final Entity progressForDay) {
        final Integer day = Integer.valueOf(progressForDay.getField(DAY).toString());

        return getDateAfterStartOrderForProgress(order, day);
    }

    public Date getDateAfterStartOrderForProgress(final Entity order, final Integer day) {
        final Date startOrder = getPlannedOrCorrectedDate(order);

        return new DateTime(startOrder).plusDays(day - 1).toDate();
    }

    private Date getPlannedOrCorrectedDate(final Entity order) {
        return order.getDateField(OrderFields.START_DATE);
    }

}
