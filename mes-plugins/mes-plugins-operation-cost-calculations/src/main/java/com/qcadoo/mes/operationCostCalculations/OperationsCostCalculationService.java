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
package com.qcadoo.mes.operationCostCalculations;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public interface OperationsCostCalculationService {

    /**
     * 
     * @param costCalculationOrProductionBalance
     *            cost calculation or production balance
     */
    void calculateOperationsCost(final Entity costCalculationOrProductionBalance);

    /**
     * 
     * @param calculationOperationComponent
     *            calculation operation component
     * @param productionCostMargin
     *            production cost margin
     * @param quantity
     *            quantity
     * @param operationTimes
     *            operation times
     * 
     * @return cost
     */
    Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode calculationOperationComponent,
            final BigDecimal productionCostMargin, final BigDecimal quantity, final OperationTimesContainer operationTimes);

    /**
     * 
     * @param calculationOperationComponent
     *            calculation operation component
     * @param productionCostMargin
     *            production cost margin
     * @param quantity
     *            quantity
     * @param operationRuns
     *            operation runs
     * 
     * @return cost
     */
    BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode calculationOperationComponent,
            final BigDecimal productionCostMargin, final BigDecimal quantity, final Map<Long, BigDecimal> operationRuns);

}
