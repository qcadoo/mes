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

import java.math.BigDecimal;
import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionCountingService {

    /**
     * Gets production tracking
     *
     * @param productionTrackingId
     * @return production tracking
     */
    Entity getProductionTracking(final Long productionTrackingId);

    /**
     * Gets production balance
     *
     * @param productionBalanceId
     * @return production balance
     */
    Entity getProductionBalance(final Long productionBalanceId);

    /**
     * Gets tracking operation product in component
     *
     * @param trackingOperationProductInComponentId
     * @return tracking operation product in component
     */
    Entity getTrackingOperationProductInComponent(final Long trackingOperationProductInComponentId);

    /**
     * Gets tracking operation product out component
     *
     * @param trackingOperationProductOutComponentId
     * @return tracking operation product out component
     */
    Entity getTrackingOperationProductOutComponent(final Long trackingOperationProductOutComponentId);

    /**
     * Gets staff work time
     *
     * @param staffWorkTimeId
     * @return staff work time
     */
    Entity getStaffWorkTime(final Long staffWorkTimeId);

    /**
     * Gets production tracking data definition
     *
     * @return production tracking data definition
     */
    DataDefinition getProductionTrackingDD();

    /**
     * Gets production balance data definition
     *
     * @return production balance data definition
     */
    DataDefinition getProductionBalanceDD();

    /**
     * Gets tracking operation product in component data definition
     *
     * @return tracking operation product in component data definition
     */
    DataDefinition getTrackingOperationProductInComponentDD();

    /**
     * Gets tracking operation product out component data definition
     *
     * @return tracking operation product out component data definition
     */
    DataDefinition getTrackingOperationProductOutComponentDD();

    /**
     * Gets staff work time data definition
     *
     * @return staff work time data definition
     */
    DataDefinition getStaffWorkTimeDD();

    /**
     * Gets production trackings for order
     *
     * @param order
     *            order
     * @return production trackings
     */
    List<Entity> getProductionTrackingsForOrder(final Entity order);

    /**
     * Is type of production recording basic
     *
     * @param typeOfProductionRecording
     *            type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingBasic(final String typeOfProductionRecording);

    /**
     * Is type of production recording for each
     *
     * @param typeOfProductionRecording
     *            type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingForEach(final String typeOfProductionRecording);

    /**
     * Is type of production recording cumulated
     *
     * @param typeOfProductionRecording
     *            type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingCumulated(final String typeOfProductionRecording);

    /**
     * Check if type of production recording is empty or basic
     *
     * @param typeOfProductionRecording
     *            type of production recording
     * @return boolean
     */
    boolean checkIfTypeOfProductionRecordingIsEmptyOrBasic(final String typeOfProductionRecording);

    /**
     * Validates order
     *
     * @param productionTrackingBalanceDD
     *            production balance data definition
     * @param productionTrackingBalance
     *            production balance
     * @return boolean
     */
    boolean validateOrder(final DataDefinition productionTrackingBalanceDD,
            final Entity productionTrackingBalance);

    /**
     * Sets components state
     *
     * @param view
     *            view
     * @param componentReferenceNames
     *            component reference names
     * @param isEnabled
     *            is enabled
     * @param requestComponentUpdateState
     *            request component update state
     */
    void setComponentsState(final ViewDefinitionState view, final List<String> componentReferenceNames, final boolean isEnabled,
            final boolean requestComponentUpdateState);

    /**
     * Changes done quantity and amount of produced quantity field state
     *
     * @param view
     *            view
     */
    void changeDoneQuantityAndAmountOfProducedQuantityFieldState(final ViewDefinitionState view);

    BigDecimal getRegisteredProductValueForOperationProductIn(final Entity operationProduct, final BigDecimal planed);

    BigDecimal getRegisteredProductValueForOperationProductOut(final Entity operationProduct, final BigDecimal planed);
}