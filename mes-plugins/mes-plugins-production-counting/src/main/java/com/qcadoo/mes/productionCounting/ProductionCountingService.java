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
package com.qcadoo.mes.productionCounting;

import java.math.BigDecimal;
import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionCountingService {

    /**
     * Gets production tracking report
     *
     * @param productionTrackingReportId
     * @return production tracking report
     */
    Entity getProductionTrackingReport(final Long productionTrackingReportId);

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
     * @param productionBalanaceId
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
     * Gets production tracking report data definition
     *
     * @return production tracking report data definition
     */
    DataDefinition getProductionTrackingReportDD();

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
     * Gets production trackings for order
     *
     * @param order order
     * @return production trackings
     */
    List<Entity> getProductionTrackingsForOrder(final Entity order);

    /**
     * Is type of production recording basic
     *
     * @param typeOfProductionRecording type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingBasic(final String typeOfProductionRecording);

    /**
     * Is type of production recording for each
     *
     * @param typeOfProductionRecording type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingForEach(final String typeOfProductionRecording);

    /**
     * Is type of production recording cumulated
     *
     * @param typeOfProductionRecording type of production recording
     * @return boolean
     */
    boolean isTypeOfProductionRecordingCumulated(final String typeOfProductionRecording);

    /**
     * Check if type of production recording is empty or basic
     *
     * @param typeOfProductionRecording type of production recording
     * @return boolean
     */
    boolean checkIfTypeOfProductionRecordingIsEmptyOrBasic(final String typeOfProductionRecording);

    /**
     * Is calculate operation cost mode hourly
     *
     * @param calculateOperationCostMode calculate operation cost mode
     * @return boolean
     */
    boolean isCalculateOperationCostModeHourly(final String calculateOperationCostMode);

    /**
     * Is calculate operation cost mode piecework
     *
     * @param calculateOperationCostMode calculate operation cost mode
     * @return boolean
     */
    boolean isCalculateOperationCostModePiecework(final String calculateOperationCostMode);

    /**
     * Validates order
     *
     * @param productionTrackingReportOrBalanceDD production tracking report or production balance data definition
     * @param productionTrackingReportOrBalance   production tracking report or production balance
     * @return boolean
     */
    boolean validateOrder(final DataDefinition productionTrackingReportOrBalanceDD,
            final Entity productionTrackingReportOrBalance);

    /**
     * Sets components state
     *
     * @param view                        view
     * @param componentReferenceNames     component reference names
     * @param isEnabled                   is enabled
     * @param requestComponentUpdateState request component update state
     */
    void setComponentsState(final ViewDefinitionState view, final List<String> componentReferenceNames, final boolean isEnabled,
            final boolean requestComponentUpdateState);

    /**
     * Sets components visibility
     *
     * @param view                        view
     * @param componentReferenceNames     component reference names
     * @param isVisible                   is visible
     * @param requestComponentUpdateState request component update state
     */
    void setComponentsVisibility(final ViewDefinitionState view, final List<String> componentReferenceNames,
            final boolean isVisible, final boolean requestComponentUpdateState);

    /**
     * Changes done quantity and amount of produced quantity field state
     *
     * @param view view
     */
    void changeDoneQuantityAndAmountOfProducedQuantityFieldState(final ViewDefinitionState view);

    /**
     * Fills fields from product
     *
     * @param view                                view
     * @param trackingOperationProductComponentDD tracking operation product component data definition
     */
    void fillFieldsFromProduct(final ViewDefinitionState view, final DataDefinition trackingOperationProductComponentDD);

    /**
     * Fills product field
     *
     * @param view view
     */
    void fillProductField(final ViewDefinitionState view);

    /**
     * Fills production trackings grid
     *
     * @param view view
     */
    void fillProductionTrackingsGrid(final ViewDefinitionState view);

    BigDecimal getRegisteredProductValueForOperationProductIn(final Entity operationProduct, final BigDecimal planed);

    BigDecimal getRegisteredProductValueForOperationProductOut(final Entity operationProduct, final BigDecimal planed);
}
