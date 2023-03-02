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
package com.qcadoo.mes.operationTimeCalculations;

import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OperationWorkTimeService {

    BigDecimal estimateAbstractOperationWorkTime(final Entity operationComponent, final BigDecimal neededNumberOfCycles,
            final boolean includeTpz, final boolean includeAdditionalTime, final BigDecimal staffFactor);

    OperationWorkTime estimateTechOperationWorkTimeForWorkstation(final Entity operationComponent,
            final BigDecimal neededNumberOfCycles, final boolean includeTpz, final boolean includeAdditionalTime,
            Entity techOperCompWorkstationTime, BigDecimal staffFactor);

    OperationWorkTime estimateOperationWorkTime(final Entity order, final Entity operationComponent, final BigDecimal neededNumberOfCycles,
                                                final boolean includeTpz, final boolean includeAdditionalTime, final boolean saved, final BigDecimal staffFactor);

    OperationTimesContainer estimateOperationsWorkTimes(final List<Entity> operationComponents,
            Map<Long, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final boolean saved);

    Entity createOrGetOperCompTimeCalculation(Entity order, Entity technologyOperationComponent);

    Entity createOrGetPlanOperCompTimeCalculation(Entity productionLineSchedule, Entity order, Entity productionLine, Entity operation);

    void deleteOperCompTimeCalculations(Entity order);

    void deletePlanOperCompTimeCalculations(Entity productionLineSchedule, Entity order, Entity productionLine);

    OperationWorkTime estimateTotalWorkTimeForTechnology(final Entity technology, final Map<Long, BigDecimal> operationRuns,
            final boolean includeTpz, final boolean includeAdditionalTime, final boolean saved);

    BigDecimal getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(Entity operationComponent, BigDecimal operationRuns,
                                                                                   BigDecimal productComponentQuantity, Entity outputProduct);

    Object setDateToField(final Date date);
}
