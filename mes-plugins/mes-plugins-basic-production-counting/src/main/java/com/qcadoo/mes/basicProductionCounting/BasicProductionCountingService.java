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
package com.qcadoo.mes.basicProductionCounting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface BasicProductionCountingService {

    /**
     * Creates production counting quantities
     * 
     * @param order
     *            order
     */
    void createProductionCountingQuantitiesAndOperationRuns(final Entity order);

    /**
     * Creates production counting operation run
     * 
     * @param order
     *            order
     * @param technologyOperationComponent
     *            technology operation component
     * @param runs
     *            runs
     * 
     * @return production counting operation run entity
     */
    Entity createProductionCountingOperationRun(final Entity order, final Entity technologyOperationComponent,
            final BigDecimal runs);

    /**
     * Creates production counting quantity
     * 
     * @param order
     *            order
     * @param technologyOperationComponent
     *            technology operation component
     * @param operationProductInComponent
     *            operation product in component
     * @param operationProductOutComponent
     *            operation product out component
     * @param product
     *            product
     * @param plannedQuantity
     *            planned quantity
     * @param isNonComponent
     *            is non component
     * 
     * @return production counting quantity entity
     */
    Entity createProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
            final Entity operationProductInComponent, final Entity operationProductOutComponent, final Entity product,
            final BigDecimal plannedQuantity, final boolean isNonComponent);

    /**
     * Updates production counting quantities
     * 
     * @param order
     *            order
     */
    void updateProductionCountingQuantitiesAndOperationRuns(final Entity order);

    /**
     * Create basic production countings
     * 
     * @param order
     *            order
     */
    void createBasicProductionCountings(final Entity order);

    /**
     * Creates basic production counting
     * 
     * @param order
     *            order
     * @param product
     *            product
     * 
     * @return basic production counting entity
     */
    Entity createBasicProductionCounting(final Entity order, final Entity product);

    /**
     * Associates production counting quantities with basic production countings
     * 
     * @param order
     *            order
     */
    void associateProductionCountingQuantitiesWithBasicProductionCountings(final Entity order);

    /**
     * Gets basic production counting
     * 
     * @param basicProductionCoutningId
     *            basicProductionCoutningId
     * 
     * @return basic production counting
     */
    Entity getBasicProductionCounting(final Long basicProductionCoutningId);

    /**
     * Gets production counting quantity
     * 
     * @param productionCountingQuantityId
     *            productionCountingQuantityId
     * 
     * @return production counting quantity
     */
    Entity getProductionCountingQuantity(final Long productionCountingQuantityId);

    /**
     * Gets basic production counting data definition
     * 
     * @return basic production counting data definition
     */
    DataDefinition getBasicProductionCountingDD();

    /**
     * Gets production counting quantity data definition
     * 
     * @return production counting quantity data definition
     */
    DataDefinition getProductionCountingQuantityDD();

    /**
     * Fills unit fields
     * 
     * @param view
     *            view
     * 
     * @param productName
     *            product lookup reference name
     * 
     * @param referenceNames
     *            reference names to unit fields
     */
    void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames);

    /**
     * Sets technology operation component field required
     * 
     * @param view
     *            view
     */
    void setTechnologyOperationComponentFieldRequired(final ViewDefinitionState view);

    /**
     * Fills row styles depends of type of material
     * 
     * @param productionCountingQuantity
     *            production counting quantity
     * 
     * @return row styles
     */
    Set<String> fillRowStylesDependsOfTypeOfMaterial(final Entity productionCountingQuantity);

}
