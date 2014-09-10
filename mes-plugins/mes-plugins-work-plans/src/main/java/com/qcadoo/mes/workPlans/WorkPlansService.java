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
package com.qcadoo.mes.workPlans;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

import java.util.List;
import java.util.Set;

public interface WorkPlansService {

    /**
     * Gets work plan with given id
     * 
     * @param workPlanId
     *            work plan id
     * 
     * @return work plan
     */
    Entity getWorkPlan(final Long workPlanId);

    /**
     * Gets work plan data definition
     * 
     * @return work plan data definition
     */
    DataDefinition getWorkPlanDD();

    /**
     * Gets work plan order column with given id
     * 
     * @param workPlanOrderColumnId
     *            work plan order column id
     * 
     * @return work plan order column
     */
    Entity getWorkPlanOrderColumn(final Long workPlanOrderColumnId);

    /**
     * Gets work plan order column data definition
     * 
     * @return work plan order column data definition
     */
    DataDefinition getWorkPlanOrderColumnDD();

    /**
     * Gets column for orders with given id
     * 
     * @param columnForOrdersId
     *            column for orders id
     * 
     * @return column for orders
     */
    Entity getColumnForOrders(final Long columnForOrdersId);

    /**
     * Gets column for orders data definition
     * 
     * @return column for orders data definition
     */
    DataDefinition getColumnForOrdersDD();

    /**
     * Gets column for input products with given id
     * 
     * @param columnForInputProductsId
     *            column for input products id
     * 
     * @return column for input products
     */
    Entity getColumnForInputProducts(final Long columnForInputProductsId);

    /**
     * Gets column for input products data definition
     * 
     * @return column for input products data definition
     */
    DataDefinition getColumnForInputProductsDD();

    /**
     * Gets column for output products with given id
     * 
     * @param columnForOutputProductsId
     *            column for output products id
     * 
     * @return column for output products
     */
    Entity getColumnForOutputProducts(final Long columnForOutputProductsId);

    /**
     * Gets column for output products data definition
     * 
     * @return column for output products data definition
     */
    DataDefinition getColumnForOutputProductsDD();

    /**
     * Gets parameter order column with given id
     * 
     * @param parameterOrderColumnId
     *            parameter order column id
     * 
     * @return parameter order column
     */
    Entity getParameterOrderColumn(final Long parameterOrderColumnId);

    /**
     * Gets parameter order column data definition
     * 
     * @return parameter order column data definition
     */
    DataDefinition getParameterOrderColumnDD();

    /**
     * Gets parameter input column with given id
     * 
     * @param parameterInputColumnId
     *            parameter input column id
     * 
     * @return parameter input column
     */
    Entity getParameterInputColumn(final Long parameterInputColumnId);

    /**
     * Gets parameter input column data definition
     * 
     * @return parameter input column data definition
     */
    DataDefinition getParameterInputColumnDD();

    /**
     * Gets parameter output column with given id
     * 
     * @param parameterOutputColumnId
     *            parameter output column id
     * 
     * @return parameter output column
     */
    Entity getParameterOutputColumn(final Long parameterOutputColumnId);

    /**
     * Gets parameter output column data definition
     * 
     * @return parameter output column data definition
     */
    DataDefinition getParameterOutputColumnDD();

    /**
     * Gets operation input column with given id
     * 
     * @param operationInputColumnId
     *            operation input column id
     * 
     * @return operation input column
     */
    Entity getOperationInputColumn(final Long operationInputColumnId);

    /**
     * Gets operation input column data definition
     * 
     * @return operation input column data definition
     */
    DataDefinition getOperationInputColumnDD();

    /**
     * Gets operation output column with given id
     * 
     * @param operationOutputColumnId
     *            operation output column id
     * 
     * @return parameter output column
     */
    Entity getOperationOutputColumn(final Long operationOutputColumnId);

    /**
     * Gets operation output column data definition
     * 
     * @return operation output column data definition
     */
    DataDefinition getOperationOutputColumnDD();

    /**
     * Gets technology operation input column with given id
     * 
     * @param technologyOperationInputColumnId
     *            technology operation input column id
     * 
     * @return technology operation input column
     */
    Entity getTechnologyOperationInputColumn(final Long technologyOperationInputColumnId);

    /**
     * Gets technology operation input column data definition
     * 
     * @return technology operation input column data definition
     */
    DataDefinition getTechnologyOperationInputColumnDD();

    /**
     * Gets technology operation output column with given id
     * 
     * @param technologyOperationOutputColumnId
     *            technology operation output column id
     * 
     * @return technology operation output column
     */
    Entity getTechnologyOperationOutputColumn(final Long technologyOperationOutputColumnId);

    /**
     * Gets technology operation output column data definition
     * 
     * @return technology operation output column data definition
     */
    DataDefinition getTechnologyOperationOutputColumnDD();

    /**
     * Generates work plan entity for given orders
     * 
     * @param orders
     *            orders
     * 
     * @return work plan
     */
    Entity generateWorkPlanEntity(final List<Entity> orders);

    /**
     * Gets selected orders with given selected order ids
     * 
     * @param selectedOrderIds
     *            selected order ids
     * 
     * @return selected orders
     */
    List<Entity> getSelectedOrders(final Set<Long> selectedOrderIds);

    /**
     * Checks attachment extension
     * 
     * @param dataDefinition
     *            data definition
     * @param attachmentFieldDef
     *            attachment field definition
     * @param entity
     *            entity
     * @param oldValue
     *            old value
     * @param newValue
     *            new value
     * 
     * @return boolean
     */
    boolean checkAttachmentExtension(final DataDefinition dataDefinition, final FieldDefinition attachmentFieldDef,
            final Entity entity, final Object oldValue, final Object newValue);

    /**
     * Checks if column is not used
     * 
     * @param componentDD
     *            component data definition
     * @param component
     *            component
     * @param modelName
     *            model name
     * @param columnName
     *            column name
     * @param componentName
     *            component name
     * 
     * @return boolean
     */
    boolean checkIfColumnIsNotUsed(final DataDefinition componentDD, final Entity component, final String modelName,
            final String columnName, final String componentName);

    /**
     * Generates name for work plan based on current date
     * 
     * @return name for work plan
     */
    String generateNameForWorkPlan();

}
