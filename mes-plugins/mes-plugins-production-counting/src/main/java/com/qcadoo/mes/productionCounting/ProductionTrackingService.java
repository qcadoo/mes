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
package com.qcadoo.mes.productionCounting;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ProductionTrackingService {

    void setTimeAndPieceworkComponentsVisible(final ViewDefinitionState view, final Entity order);

    ProductionTrackingState getTrackingState(final ViewDefinitionState view);

    void fillProductionLineLookup(final ViewDefinitionState view);

    void changeState(Entity productionTracking, ProductionTrackingState state);

    Entity correct(Entity productionTracking);

    void unCorrect(Entity productionTracking, boolean updateOrderReportedQuantity);

    Optional<BigDecimal> calculateGivenQuantity(Entity trackingOperationProductInComponent, BigDecimal usedQuantity);

    Either<Boolean,Optional<Date>> findExpirationDate(final Entity productionTracking, final Entity order, final Entity toc, final Entity batch);

    BigDecimal getTrackedQuantity(Entity trackingOperationProductOutComponent, List<Entity> trackings, boolean useTracking);

    List<Entity> findTrackingOperationProductOutComponents(Entity order, Entity toc, Entity product);
}
