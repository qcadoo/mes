/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionCountingService {

    /**
     * Gets production counting
     * 
     * @param productionCountingId
     * 
     * @return production counting
     */
    Entity getProductionCounting(final Long productionCountingId);

    /**
     * Gets production record
     * 
     * @param productionRecordId
     * 
     * @return production record
     */
    Entity getProductionRecord(final Long productionCountingId);

    /**
     * Gets production balance
     * 
     * @param productionBalanaceId
     * 
     * @return production balance
     */
    Entity getProductionBalance(final Long productionBalanceId);

    /**
     * Gets production counting data definition
     * 
     * @return production counting data definition
     */
    DataDefinition getProductionCountingDD();

    /**
     * Gets production record data definition
     * 
     * @return production record data definition
     */
    DataDefinition getProductionRecordDD();

    /**
     * Gets production balance data definition
     * 
     * @return production balance data definition
     */
    DataDefinition getProductionBalanceDD();

    /**
     * Gets production records for order
     * 
     * @param order
     *            order
     * @return production records
     */
    List<Entity> getProductionRecordsForOrder(final Entity order);

    /**
     * Is type of production recording basic
     * 
     * @param typeOfProductionRecording
     *            type of production recording
     * 
     * @return boolean
     */
    boolean isTypeOfProductionRecordingBasic(final String typeOfProductionRecording);

    /**
     * Is type of production recording for each
     * 
     * @param typeOfProductionRecording
     *            type of production recording
     * 
     * @return boolean
     */
    boolean isTypeOfProductionRecordingForEach(final String typeOfProductionRecording);

    /**
     * Is type of production recording cumulated
     * 
     * @param typeOfProductionRecording
     *            type of production recording
     * 
     * @return boolean
     */
    boolean isTypeOfProductionRecordingCumulated(final String typeOfProductionRecording);

    /**
     * Check if type of production recording is empty or basic
     * 
     * @param typeOfProductionRecording
     *            type of production recording
     * 
     * @return boolean
     */
    boolean checkIfTypeOfProductionRecordingIsEmptyOrBasic(final String typeOfProductionRecording);

    /**
     * Is calculate operation cost mode hourly
     * 
     * @param calculateOperationCostMode
     *            calculate operation cost mode
     * 
     * @return boolean
     */
    boolean isCalculateOperationCostModeHourly(final String calculateOperationCostMode);

    /**
     * Is calculate operation cost mode piecework
     * 
     * @param calculateOperationCostMode
     *            calculate operation cost mode
     * 
     * @return boolean
     */
    boolean isCalculateOperationCostModePiecework(final String calculateOperationCostMode);

    /**
     * Validates order
     * 
     * @param productionCountingOrBalanceDD
     *            production counting or production balance data definition
     * @param productionCountingOrBalance
     *            production counting or production balance
     * 
     * @return boolean
     */
    boolean validateOrder(final DataDefinition productionCountingOrBalanceDD, final Entity productionCountingOrBalance);

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
     * Sets components visibility
     * 
     * @param view
     *            view
     * @param componentReferenceNames
     *            component reference names
     * @param isVisible
     *            is visible
     * @param requestComponentUpdateState
     *            request component update state
     */
    void setComponentsVisibility(final ViewDefinitionState view, final List<String> componentReferenceNames,
            final boolean isVisible, final boolean requestComponentUpdateState);

    /**
     * Changes done quantity and amount of produced quantity field state
     * 
     * @param view
     *            view
     */
    void changeDoneQuantityAndAmountOfProducedQuantityFieldState(final ViewDefinitionState view);

    /**
     * Fills fields from product
     * 
     * @param view
     *            view
     */
    void fillFieldsFromProduct(final ViewDefinitionState view);

    /**
     * Fills product field
     * 
     * @param view
     *            view
     */
    void fillProductField(final ViewDefinitionState view);

    /**
     * Fills production records grid
     * 
     * @param view
     *            view
     */
    void fillProductionRecordsGrid(final ViewDefinitionState view);

}
