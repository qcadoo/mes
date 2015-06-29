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
package com.qcadoo.mes.productionPerShift.util;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.dataProvider.ProductionPerShiftDataProvider;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProgressQuantitiesDeviationNotifier {

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionPerShiftDataProvider productionPerShiftDataProvider;

    public void compareAndNotify(final ViewDefinitionState view, final Entity order, final Entity technologyOperationComponent) {
        Optional<BigDecimal> maybeQuantitiesDifference = calculateQuantitiesDifference(order, technologyOperationComponent);
        for (BigDecimal quantitiesDifference : maybeQuantitiesDifference.asSet()) {
            int compareResult = quantitiesDifference.compareTo(BigDecimal.ZERO);
            if (compareResult > 0) {
                showQuantitiesDeviationNotice(view, quantitiesDifference,
                        "productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSSmaller");
            } else if (compareResult < 0) {
                showQuantitiesDeviationNotice(view, quantitiesDifference,
                        "productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSGreater");
            }
        }
    }

    private Optional<BigDecimal> calculateQuantitiesDifference(final Entity order, final Entity technologyOperationComponent) {
        if (technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            return Optional.absent();
        }
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;
        BigDecimal sumOfDailyPlannedQuantities = productionPerShiftDataProvider.countSumOfQuantities(technology.getId(),
                ProductionPerShiftDataProvider.ONLY_ROOT_OPERATIONS_CRITERIA, shouldBeCorrected);
        BigDecimal planedQuantityFromOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        return Optional.of(planedQuantityFromOrder.subtract(sumOfDailyPlannedQuantities, numberService.getMathContext()));
    }

    private void showQuantitiesDeviationNotice(final ViewDefinitionState view, final BigDecimal quantitiesDifference,
            final String messageKey) {
        for (ComponentState productionPerShiftForm : view.tryFindComponentByReference("form").asSet()) {
            productionPerShiftForm.addMessage(messageKey, ComponentState.MessageType.INFO, false,
                    numberService.formatWithMinimumFractionDigits(quantitiesDifference.abs(numberService.getMathContext()), 0));
        }
    }

}
