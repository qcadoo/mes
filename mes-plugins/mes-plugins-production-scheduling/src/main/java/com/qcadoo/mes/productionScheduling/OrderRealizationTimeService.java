/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionScheduling;

import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.Entity;

import java.util.Optional;

public interface OrderRealizationTimeService {

    int MAX_REALIZATION_TIME = 99999 * 60 * 60;

    /**
     * @param operationComponent    operationComponent of an operation we want to estimate.
     * @param includeTpz            Flag indicating if we want to include Tpz
     * @param includeAdditionalTime Flag indicating if we want to include Additional Time
     * @param productionLine        production line for technology. It's needed to retrieve workstations info. It's not used if we deal with an
     *                              order, though.
     * @return Time consumption of an operation in seconds, including offset caused by waiting for child operations to finish.
     */
    int estimateOperationTimeConsumption(final Entity productionLineSchedule, final Entity order,
                                         final Entity operationComponent,
                                         final boolean includeTpz, final boolean includeAdditionalTime,
                                         boolean maxForWorkstation,
                                         final Entity productionLine,
                                         Optional<ProductQuantitiesHolder> productQuantitiesAndOperationRuns);

    OperationWorkTime estimateTotalWorkTimeForOrder(final Entity order, final boolean includeTpz,
                                                    final boolean includeAdditionalTime, final boolean saved);

}
