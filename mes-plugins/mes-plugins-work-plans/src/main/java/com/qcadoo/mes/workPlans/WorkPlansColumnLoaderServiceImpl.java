/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.workPlans.constants.ColumnForProductsFields.ACTIVE;
import static com.qcadoo.mes.workPlans.constants.ColumnForProductsFields.TYPE;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.OperationType;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WorkPlansColumnLoaderServiceImpl implements WorkPlansColumnLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlansColumnLoaderServiceImpl.class);

    private static final String L_TRUE = "true";

    private static final String L_COLUMN_FOR_ORDERS = "columnForOrders";

    private static final String L_COLUMN_FOR_PRODUCTS = "columnForProducts";

    private enum ColumnType {
        INPUT("input"), OUTPUT("output"), BOTH("both");

        private String stringValue;

        private ColumnType(final String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            return stringValue;
        }
    };

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setParameterDefaultValues() {
        Entity parameter = parameterService.getParameter();

        for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
            if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                continue;
            }

            parameter.setField(workPlanParameter, false);
        }

        parameter.getDataDefinition().save(parameter);

        if (parameter.isValid() && LOG.isDebugEnabled()) {
            LOG.debug("Parameter saved {column=" + parameter.toString() + "}");
        }
    }

    public void setOperationDefaultValues() {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                    if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                        continue;
                    }

                    operation.setField(workPlanParameter, false);
                }

                operation.getDataDefinition().save(operation);

                if (operation.isValid() && LOG.isDebugEnabled()) {
                    LOG.debug("Operation saved {column=" + operation.toString() + "}");
                }
            }
        }
    }

    public void setTechnologyOperationComponentDefaultValues() {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                    if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                        continue;
                    }

                    technologyOperationComponent.setField(workPlanParameter, false);
                }

                technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);

                if (technologyOperationComponent.isValid() && LOG.isDebugEnabled()) {
                    LOG.debug("Technology Operation Component saved {column=" + technologyOperationComponent.toString() + "}");
                }
            }
        }

    }

    public void setTechnologyInstanceOperationComponentDefaultValues() {
        List<Entity> technologyInstanceOperationComponents = getTechnologyInstanceOperationComponents();

        if (technologyInstanceOperationComponents != null) {
            for (Entity technologyInstanceOperationComponent : technologyInstanceOperationComponents) {
                for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                    if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                        continue;
                    }

                    technologyInstanceOperationComponent.setField(workPlanParameter, false);
                }

                technologyInstanceOperationComponent.getDataDefinition().save(technologyInstanceOperationComponent);

                if (technologyInstanceOperationComponent.isValid() && LOG.isDebugEnabled()) {
                    LOG.debug("Technology Instance Operation Component saved {column="
                            + technologyInstanceOperationComponent.toString() + "}");
                }
            }
        }
    }

    public void fillColumnsForOrders(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_ORDERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_ORDERS, OperationType.ADD, columnAttributes);
        }
    }

    public void clearColumnsForOrders(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_ORDERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_ORDERS, OperationType.DELETE, columnAttributes);
        }
    }

    public void fillColumnsForProducts(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_PRODUCTS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_PRODUCTS, OperationType.ADD, columnAttributes);
        }
    }

    public void clearColumnsForProducts(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_PRODUCTS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_PRODUCTS, OperationType.DELETE, columnAttributes);
        }
    }

    private void readData(final String model, final OperationType operation, final Map<String, String> columnAttributes) {
        if (L_COLUMN_FOR_ORDERS.equals(model)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForOrders(columnAttributes);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForOrders(columnAttributes);
            }
        } else if (L_COLUMN_FOR_PRODUCTS.equals(model)) {
            if (OperationType.ADD.equals(operation)) {
                if (ColumnType.BOTH.getStringValue().equals(columnAttributes.get(TYPE))) {
                    addColumnForInputProducts(columnAttributes);
                    addColumnForOutputProducts(columnAttributes);
                } else if (ColumnType.INPUT.getStringValue().equals(columnAttributes.get(TYPE))) {
                    addColumnForInputProducts(columnAttributes);
                } else if (ColumnType.OUTPUT.getStringValue().equals(columnAttributes.get(TYPE))) {
                    addColumnForOutputProducts(columnAttributes);
                } else {
                    throw new IllegalStateException("Incorrect type - " + columnAttributes.get(TYPE));
                }
            } else if (OperationType.DELETE.equals(operation)) {
                if (ColumnType.BOTH.getStringValue().equals(columnAttributes.get(TYPE))) {
                    deleteColumnForInputProducts(columnAttributes);
                    deleteColumnForOutputProducts(columnAttributes);
                } else if (ColumnType.INPUT.getStringValue().equals(columnAttributes.get(TYPE))) {
                    deleteColumnForInputProducts(columnAttributes);
                } else if (ColumnType.OUTPUT.getStringValue().equals(columnAttributes.get(TYPE))) {
                    deleteColumnForOutputProducts(columnAttributes);
                } else {
                    throw new IllegalStateException("Incorrect type - " + columnAttributes.get(TYPE));
                }
            }
        }
    }

    private void addColumnForOrders(final Map<String, String> columnAttributes) {
        Entity columnForOrders = columnExtensionService.addColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnAttributes);

        if (L_TRUE.equals(columnAttributes.get(ACTIVE))) {
            addParameterOrderColumn(columnForOrders);
            addWorkPlanOrderColumn(columnForOrders);
        }
    }

    private void deleteColumnForOrders(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS,
                columnAttributes);
    }

    private void addColumnForInputProducts(final Map<String, String> columnAttributes) {
        Entity columnForInputProducts = columnExtensionService.addColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnAttributes);

        if (L_TRUE.equals(columnAttributes.get(ACTIVE))) {
            addParameterInputColumn(columnForInputProducts);
            addOperationInputColumn(columnForInputProducts);
            addTechnologyOperationInputColumn(columnForInputProducts);
            addOrderOperationInputColumn(columnForInputProducts);
        }
    }

    private void deleteColumnForInputProducts(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnAttributes);
    }

    private void addColumnForOutputProducts(final Map<String, String> columnAttributes) {
        Entity columnForOutputProducts = columnExtensionService.addColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnAttributes);

        if (L_TRUE.equals(columnAttributes.get(ACTIVE))) {
            addParameterOutputColumn(columnForOutputProducts);
            addOperationOutputColumn(columnForOutputProducts);
            addTechnologyOperationOutputColumn(columnForOutputProducts);
            addOrderOperationOutputColumn(columnForOutputProducts);
        }
    }

    private void deleteColumnForOutputProducts(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnAttributes);
    }

    private void addParameterOrderColumn(final Entity columnForOrders) {
        Entity parameterOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_ORDER_COLUMN).create();

        parameterOrderColumn.setField(BasicConstants.MODEL_PARAMETER, parameterService.getParameter());
        parameterOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

        parameterOrderColumn = parameterOrderColumn.getDataDefinition().save(parameterOrderColumn);

        if (parameterOrderColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter order columns {column=" + parameterOrderColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrderColumn - has validation errors - "
                    + columnForOrders.toString());
        }
    }

    private void addWorkPlanOrderColumn(final Entity columnForOrders) {
        for (Entity workPlan : getWorkPlans()) {
            Entity workPlanOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_WORK_PLAN_ORDER_COLUMN).create();

            workPlanOrderColumn.setField(WorkPlansConstants.MODEL_WORK_PLAN, workPlan);
            workPlanOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

            workPlanOrderColumn = workPlanOrderColumn.getDataDefinition().save(workPlanOrderColumn);

            if (workPlanOrderColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to work plan order columns {column=" + workPlanOrderColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - workPlanOrderColumn - has validation errors - "
                        + columnForOrders.toString());
            }
        }
    }

    private void addParameterInputColumn(final Entity columnForInputProducts) {
        Entity parameter = parameterService.getParameter();
        Entity parameterInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_INPUT_COLUMN).create();

        parameterInputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        parameterInputColumn = parameterInputColumn.getDataDefinition().save(parameterInputColumn);

        if (parameterInputColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter input columns {column=" + parameterInputColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterInputColumn - has validation errors - "
                    + columnForInputProducts.toString());
        }
    }

    private void addParameterOutputColumn(final Entity columnForOutputProducts) {
        Entity parameter = parameterService.getParameter();
        Entity parameterOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COLUMN).create();

        parameterOutputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

        parameterOutputColumn = parameterOutputColumn.getDataDefinition().save(parameterOutputColumn);

        if (parameterOutputColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter output columns {column=" + parameterOutputColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOutputColumn - has validation errors - "
                    + columnForOutputProducts.toString());
        }
    }

    private void addOperationInputColumn(final Entity columnForInputProducts) {
        for (Entity operation : getOperations()) {
            Entity operationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_OPERATION_INPUT_COLUMN).create();

            operationInputColumn.setField(TechnologiesConstants.MODEL_OPERATION, operation);
            operationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            operationInputColumn = operationInputColumn.getDataDefinition().save(operationInputColumn);

            if (operationInputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to operation input columns {column=" + operationInputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - operationInputColumn - has validation errors - "
                        + columnForInputProducts.toString());
            }
        }
    }

    private void addOperationOutputColumn(final Entity columnForOutputProducts) {
        for (Entity operation : getOperations()) {
            Entity operationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_OPERATION_OUTPUT_COLUMN).create();

            operationOutputColumn.setField(TechnologiesConstants.MODEL_OPERATION, operation);
            operationOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            operationOutputColumn = operationOutputColumn.getDataDefinition().save(operationOutputColumn);

            if (operationOutputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to operation output columns {column=" + operationOutputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - operationOutputColumn - has validation errors - "
                        + columnForOutputProducts.toString());
            }
        }
    }

    private void addTechnologyOperationInputColumn(final Entity columnForInputProducts) {
        for (Entity technologyOperationComponent : getTechnologyOperationComponents()) {
            Entity technologyOperationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_INPUT_COLUMN).create();

            technologyOperationInputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            technologyOperationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            technologyOperationInputColumn = technologyOperationInputColumn.getDataDefinition().save(
                    technologyOperationInputColumn);

            if (technologyOperationInputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to technology operation input columns {column="
                            + technologyOperationInputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - technologyOperationInputColumn - has validation errors - "
                        + columnForInputProducts.toString());
            }
        }
    }

    private void addTechnologyOperationOutputColumn(final Entity columnForOutputProducts) {
        for (Entity technologyOperationComponent : getTechnologyOperationComponents()) {
            Entity technologyOperationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_OUTPUT_COLUMN).create();

            technologyOperationOutputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            technologyOperationOutputColumn
                    .setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            technologyOperationOutputColumn = technologyOperationOutputColumn.getDataDefinition().save(
                    technologyOperationOutputColumn);

            if (technologyOperationOutputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to technology operation output columns {column="
                            + technologyOperationOutputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - technologyOperationOutputColumn - has validation errors - "
                        + columnForOutputProducts.toString());
            }
        }
    }

    private void addOrderOperationInputColumn(final Entity columnForInputProducts) {
        for (Entity technologyInstanceOperationComponent : getTechnologyInstanceOperationComponents()) {
            Entity orderOperationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COLUMN).create();

            orderOperationInputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                    technologyInstanceOperationComponent);
            orderOperationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            orderOperationInputColumn = orderOperationInputColumn.getDataDefinition().save(orderOperationInputColumn);

            if (orderOperationInputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to order operation input columns {column=" + orderOperationInputColumn.toString()
                            + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - orderOperationInputColumn - has validation errors - "
                        + columnForInputProducts.toString());
            }
        }
    }

    private void addOrderOperationOutputColumn(final Entity columnForOutputProducts) {
        for (Entity technologyInstanceOperationComponent : getTechnologyInstanceOperationComponents()) {
            Entity orderOperationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COLUMN).create();

            orderOperationOutputColumn.setField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                    technologyInstanceOperationComponent);
            orderOperationOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            orderOperationOutputColumn = orderOperationOutputColumn.getDataDefinition().save(orderOperationOutputColumn);

            if (orderOperationOutputColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to order operation output columns {column=" + orderOperationOutputColumn.toString()
                            + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - orderOperationOutputColumn - has validation errors - "
                        + columnForOutputProducts.toString());
            }
        }
    }

    private List<Entity> getWorkPlans() {
        List<Entity> workPlans = dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN).find().list().getEntities();

        if (workPlans == null) {
            return Lists.newArrayList();
        } else {
            return workPlans;
        }
    }

    private List<Entity> getOperations() {
        List<Entity> operations = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION).find().list().getEntities();

        if (operations == null) {
            return Lists.newArrayList();
        } else {
            return operations;
        }
    }

    private List<Entity> getTechnologyOperationComponents() {
        List<Entity> technologyOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .list().getEntities();

        if (technologyOperationComponents == null) {
            return Lists.newArrayList();
        } else {
            return technologyOperationComponents;
        }
    }

    private List<Entity> getTechnologyInstanceOperationComponents() {
        List<Entity> technologyInstanceOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .find().list().getEntities();

        if (technologyInstanceOperationComponents == null) {
            return Lists.newArrayList();
        } else {
            return technologyInstanceOperationComponents;
        }
    }

    public boolean isColumnsForOrdersEmpty() {
        return columnExtensionService.isColumnsEmpty(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS);
    }

    public boolean isColumnsForProductsEmpty() {
        return columnExtensionService.isColumnsEmpty(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS)
                && columnExtensionService.isColumnsEmpty(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS);
    }

}
