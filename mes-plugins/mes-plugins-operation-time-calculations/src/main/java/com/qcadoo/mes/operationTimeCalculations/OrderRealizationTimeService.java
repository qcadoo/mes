/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface OrderRealizationTimeService {

    int MAX_REALIZATION_TIME = 99999 * 60 * 60;

    void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    BigDecimal getBigDecimalFromField(final Object value, final Locale locale);

    /**
     * 
     * @param operationComponent
     *            operationComponent of an operation we want to estimate. Can be either technologyOperationComponent or
     *            orderOperationComponent
     * @param plannedQuantity
     *            How many products we want this operation to produce
     * @return Duration of an operation in seconds, including offset caused by waiting for child operations to finish
     *         (includeTpz).
     */
    int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity);

    /**
     * 
     * @param operationComponent
     *            operationComponent of an operation we want to estimate. Can be either technologyOperationComponent or
     *            orderOperationComponent
     * @param plannedQuantity
     *            How many products we want this operation to produce
     * @param includeTpz
     *            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime
     *            Flag indicating if we want to include Additional Time
     * @return Duration of an operation in seconds, including offset caused by waiting for child operations to finish.
     */
    int estimateRealizationTimeForOperation(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime);

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
     * @return Map where keys are operationComponents and values are corresponding operation durations (just operation durations,
     *         without offset added)
     */
    Map<Entity, Integer> estimateRealizationTimes(final Entity entity, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime);

    Object setDateToField(final Date date);

}