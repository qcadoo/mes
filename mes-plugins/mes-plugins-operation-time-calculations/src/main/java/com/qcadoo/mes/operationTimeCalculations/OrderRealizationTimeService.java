/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.operationTimeCalculations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;

public interface OrderRealizationTimeService {

    int MAX_REALIZATION_TIME = 99999 * 60 * 60;

    BigDecimal getBigDecimalFromField(final Object value, final Locale locale);

    /**
     * 
     * @param operationComponent
     *            operationComponent of an operation we want to estimate. Can be either technologyOperationComponent or
     *            technologyInstanceOperationComponent
     * @param plannedQuantity
     *            How many products we want this operation to produce
     * @param productionLine
     *            production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *            order, though.
     * @return Time consumption of an operation in seconds, including offset caused by waiting for child operations to finish.
     */
    int estimateOperationTimeConsumption(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final Entity productionLine);

    /**
     * 
     * @param operationComponent
     *            operationComponent of an operation we want to estimate. Can be either technologyOperationComponent or
     *            technologyInstanceOperationComponent
     * @param plannedQuantity
     *            How many products we want this operation to produce
     * @param includeTpz
     *            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime
     *            Flag indicating if we want to include Additional Time
     * @param productionLine
     *            production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *            order, though.
     * @return Time consumption of an operation in seconds, including offset caused by waiting for child operations to finish.
     */
    int estimateOperationTimeConsumption(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine);

    /**
     * 
     * @param operationComponent
     *            operationComponent of an operation we want to estimate. Can be either technologyOperationComponent or
     *            technologyInstanceOperationComponent
     * @param plannedQuantity
     *            How many products we want this operation to produce
     * @param includeTpz
     *            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime
     *            Flag indicating if we want to include Additional Time
     * @param productionLine
     *            production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *            order, though.
     * @return Max time consumption for workstation of an operation in seconds, including offset caused by waiting for child
     *         operations to finish.
     */
    int estimateMaxOperationTimeConsumptionForWorkstation(EntityTreeNode operationComponent, BigDecimal plannedQuantity,
            boolean includeTpz, boolean includeAdditionalTime, Entity productionLine);

    /**
     * 
     * @param entity
     *            An order or a technology for which we want to estimate operation times.
     * @param plannedQuantity
     *            How many products we want this order/technology to produce
     * @param includeTpz
     *            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime
     *            Flag indicating if we want to include Additional Time
     * @param productionLine
     *            production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *            order, though.
     * @return Map where keys are operationComponents and values are corresponding operation time consumptions
     */
    Map<Entity, Integer> estimateOperationTimeConsumptions(final Entity entity, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine);

    /**
     * 
     * @param entity
     *            An order or a technology for which we want to estimate operation times.
     * @param plannedQuantity
     *            How many products we want this order/technology to produce
     * @param includeTpz
     *            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime
     *            Flag indicating if we want to include Additional Time
     * @param productionLine
     *            production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *            order, though.
     * @return Map where keys are operationComponents and values are corresponding max operation time consumptions for workstation
     *         (realization times I guess)
     */
    Map<Entity, Integer> estimateMaxOperationTimeConsumptionsForWorkstations(final Entity entity,
            final BigDecimal plannedQuantity, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine);

    Object setDateToField(final Date date);
}