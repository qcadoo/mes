/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class PPSHelper {

    private static final String L_PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long getPpsIdForOrder(final Long orderId) {
        DataDefinition ppsDateDef = getProductionPerShiftDD();
        String query = "select id as ppsId from #productionPerShift_productionPerShift where order.id = :orderId";
        Entity projectionResults = ppsDateDef.find(query).setLong("orderId", orderId).setMaxResults(1).uniqueResult();

        if (projectionResults == null) {
            return null;
        }

        return (Long) projectionResults.getField("ppsId");
    }

    public Long createPpsForOrderAndReturnId(final Long orderId) {
        DataDefinition productionPerShiftDD = getProductionPerShiftDD();

        Entity productionPerShift = productionPerShiftDD.create();
        productionPerShift.setField(ProductionPerShiftFields.ORDER, orderId);
        productionPerShift.setField(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE, PlannedProgressType.PLANNED.getStringValue());

        return productionPerShiftDD.save(productionPerShift).getId();
    }

    public DataDefinition getProductionPerShiftDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
    }

    public DataDefinition getDailyProgressDD() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }

    public Entity getTechnologyOperationComponentFromOperationLookup(final ViewDefinitionState view) {
        LookupComponent operationLookup = (LookupComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        Long id = (Long) operationLookup.getFieldValue();
        Entity technologyOperationComponent = null;

        if (id != null) {
            technologyOperationComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(id);
        }

        return technologyOperationComponent;
    }

    public boolean shouldHasCorrections(final ViewDefinitionState viewState) {
        FieldComponent plannedProgressTypeField = (FieldComponent) viewState
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);

        return PlannedProgressType.CORRECTED.getStringValue().equals(plannedProgressTypeField.getFieldValue());
    }

    public Date getDateAfterStartOrderForProgress(final Entity order, final Entity progressForDay) {
        final Integer day = Integer.valueOf(progressForDay.getField(ProgressForDayFields.DAY).toString());

        return getDateAfterStartOrderForProgress(order, day);
    }

    public Date getDateAfterStartOrderForProgress(final Entity order, final Integer day) {
        final Date startOrder = order.getDateField(OrderFields.START_DATE);

        return new DateTime(startOrder).plusDays(day - 1).toDate();
    }

}
