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
package com.qcadoo.mes.workPlans;

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
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WorkPlansColumnLoaderServiceImpl implements WorkPlansColumnLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlansColumnLoaderServiceImpl.class);

    private static final String L_ACTIVE = "active";

    private static final String L_TYPE = "type";

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
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private WorkPlansService workPlansService;

    public void setParameterDefaultValues() {
        Entity parameter = parameterService.getParameter();

        for (String fieldName : Lists.newArrayList(ParameterFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS,
                ParameterFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS, ParameterFieldsWP.IMAGE_URL_IN_WORK_PLAN,
                ParameterFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS,
                ParameterFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS)) {
            if (fieldName.equals(ParameterFieldsWP.IMAGE_URL_IN_WORK_PLAN)) {
                continue;
            }

            parameter.setField(fieldName, false);
        }

        parameter.getDataDefinition().save(parameter);

        if (parameter.isValid() && LOG.isDebugEnabled()) {
            LOG.debug("Parameter saved {parameter = " + parameter.toString() + "}");
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
                if (ColumnType.BOTH.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    addColumnForInputProducts(columnAttributes);
                    addColumnForOutputProducts(columnAttributes);
                } else if (ColumnType.INPUT.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    addColumnForInputProducts(columnAttributes);
                } else if (ColumnType.OUTPUT.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    addColumnForOutputProducts(columnAttributes);
                } else {
                    throw new IllegalStateException("Incorrect type - " + columnAttributes.get(L_TYPE));
                }
            } else if (OperationType.DELETE.equals(operation)) {
                if (ColumnType.BOTH.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    deleteColumnForInputProducts(columnAttributes);
                    deleteColumnForOutputProducts(columnAttributes);
                } else if (ColumnType.INPUT.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    deleteColumnForInputProducts(columnAttributes);
                } else if (ColumnType.OUTPUT.getStringValue().equals(columnAttributes.get(L_TYPE))) {
                    deleteColumnForOutputProducts(columnAttributes);
                } else {
                    throw new IllegalStateException("Incorrect type - " + columnAttributes.get(L_TYPE));
                }
            }
        }
    }

    private void addColumnForOrders(final Map<String, String> columnAttributes) {
        Entity columnForOrders = columnExtensionService.addColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnAttributes);

        if (L_TRUE.equals(columnAttributes.get(L_ACTIVE))) {
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

        if (L_TRUE.equals(columnAttributes.get(L_ACTIVE))) {
            addParameterInputColumn(columnForInputProducts);
        }
    }

    private void deleteColumnForInputProducts(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnAttributes);
    }

    private void addColumnForOutputProducts(final Map<String, String> columnAttributes) {
        Entity columnForOutputProducts = columnExtensionService.addColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnAttributes);

        if (L_TRUE.equals(columnAttributes.get(L_ACTIVE))) {
            addParameterOutputColumn(columnForOutputProducts);
        }
    }

    private void deleteColumnForOutputProducts(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnAttributes);
    }

    private void addParameterOrderColumn(final Entity columnForOrders) {
        Entity parameterOrderColumn = workPlansService.getParameterOrderColumnDD().create();

        parameterOrderColumn.setField(BasicConstants.MODEL_PARAMETER, parameterService.getParameter());
        parameterOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

        parameterOrderColumn = parameterOrderColumn.getDataDefinition().save(parameterOrderColumn);

        if (parameterOrderColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter order columns {parameterOrderColumn = " + parameterOrderColumn.toString()
                        + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrderColumn - has validation errors - "
                    + columnForOrders.toString());
        }
    }

    private void addWorkPlanOrderColumn(final Entity columnForOrders) {
        for (Entity workPlan : getWorkPlans()) {
            Entity workPlanOrderColumn = workPlansService.getWorkPlanOrderColumnDD().create();

            workPlanOrderColumn.setField(WorkPlansConstants.MODEL_WORK_PLAN, workPlan);
            workPlanOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

            workPlanOrderColumn = workPlanOrderColumn.getDataDefinition().save(workPlanOrderColumn);

            if (workPlanOrderColumn.isValid()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to work plan order columns {workPlanOrderColumn = " + workPlanOrderColumn.toString()
                            + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - workPlanOrderColumn - has validation errors - "
                        + columnForOrders.toString());
            }
        }
    }

    private void addParameterInputColumn(final Entity columnForInputProducts) {
        Entity parameter = parameterService.getParameter();
        Entity parameterInputColumn = workPlansService.getParameterInputColumnDD().create();

        parameterInputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        parameterInputColumn = parameterInputColumn.getDataDefinition().save(parameterInputColumn);

        if (parameterInputColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter input columns {parameterInputColumn = " + parameterInputColumn.toString()
                        + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterInputColumn - has validation errors - "
                    + columnForInputProducts.toString());
        }
    }

    private void addParameterOutputColumn(final Entity columnForOutputProducts) {
        Entity parameter = parameterService.getParameter();
        Entity parameterOutputColumn = workPlansService.getParameterOutputColumnDD().create();

        parameterOutputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

        parameterOutputColumn = parameterOutputColumn.getDataDefinition().save(parameterOutputColumn);

        if (parameterOutputColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter output columns {parameterOutputColumn = " + parameterOutputColumn.toString()
                        + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOutputColumn - has validation errors - "
                    + columnForOutputProducts.toString());
        }
    }

    private List<Entity> getWorkPlans() {
        List<Entity> workPlans = workPlansService.getWorkPlanDD().find().list().getEntities();

        if (workPlans == null) {
            return Lists.newArrayList();
        } else {
            return workPlans;
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
