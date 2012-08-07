/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkPlansColumnLoaderServiceImpl implements WorkPlansColumnLoaderService {

    private static final String L_TRUE = "true";

    private static final String L_COLUMN_FOR_ORDERS = "columnForOrders";

    private static final String L_COLUMN_FOR_PRODUCTS = "columnForProducts";

    private static final String L_IDENTIFIER = "identifier";

    private static final String L_NAME = "name";

    private static final String L_DESCRIPTION = "description";

    private static final String L_COLUMNFILLER = "columnFiller";

    private static final String L_TYPE = "type";

    private static final String L_ALIGNMENT = "alignment";

    private static final String L_ACTIVE = "active";

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

    private static enum OperationType {
        ADD, DELETE;
    };

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlansColumnLoaderServiceImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

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
        readDataFromXML(plugin, L_COLUMN_FOR_ORDERS, OperationType.ADD);
    }

    public void clearColumnsForOrders(final String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_ORDERS, OperationType.DELETE);
    }

    public void fillColumnsForProducts(final String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_PRODUCTS, OperationType.ADD);
    }

    public void clearColumnsForProducts(final String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_PRODUCTS, OperationType.DELETE);
    }

    private void readDataFromXML(final String plugin, final String type, final OperationType operation) {
        LOG.info("Loading test data from " + type + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(plugin, type));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {
                Element node = list.get(i);
                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtribute = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();

                for (int j = 0; j < listOfAtribute.size(); j++) {
                    values.put(listOfAtribute.get(j).getName().toLowerCase(Locale.ENGLISH), listOfAtribute.get(j).getValue());
                }
                readData(type, operation, values);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String type, final OperationType operation, final Map<String, String> values) {
        if (L_COLUMN_FOR_ORDERS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForOrders(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForOrders(values);
            }
        } else if (L_COLUMN_FOR_PRODUCTS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                if (ColumnType.BOTH.getStringValue().equals(values.get(L_TYPE))) {
                    addColumnForInputProducts(values);
                    addColumnForOutputProducts(values);
                } else if (ColumnType.INPUT.getStringValue().equals(values.get(L_TYPE))) {
                    addColumnForInputProducts(values);
                } else if (ColumnType.OUTPUT.getStringValue().equals(values.get(L_TYPE))) {
                    addColumnForOutputProducts(values);
                } else {
                    throw new IllegalStateException("Incorrect type - " + values.get(L_TYPE));
                }
            } else if (OperationType.DELETE.equals(operation)) {
                if (ColumnType.BOTH.getStringValue().equals(values.get(L_TYPE))) {
                    deleteColumnForInputProducts(values);
                    deleteColumnForOutputProducts(values);
                } else if (ColumnType.INPUT.getStringValue().equals(values.get(L_TYPE))) {
                    deleteColumnForInputProducts(values);
                } else if (ColumnType.OUTPUT.getStringValue().equals(values.get(L_TYPE))) {
                    deleteColumnForOutputProducts(values);
                } else {
                    throw new IllegalStateException("Incorrect type - " + values.get(L_TYPE));
                }
            }
        }
    }

    private void addColumnForOrders(final Map<String, String> values) {
        Entity columnForOrders = getColumnForOrdersDD().create();

        columnForOrders.setField(L_IDENTIFIER, values.get(L_IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(L_NAME, values.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(L_DESCRIPTION, values.get(L_DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(L_COLUMNFILLER, values.get(L_COLUMNFILLER.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(L_ALIGNMENT, values.get(L_ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for orders item {column=" + columnForOrders.getStringField(L_NAME) + "}");
        }

        columnForOrders = columnForOrders.getDataDefinition().save(columnForOrders);

        if (columnForOrders.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for input products saved {column=" + columnForOrders.toString() + "}");
            }

            if (L_TRUE.equals(values.get(L_ACTIVE))) {
                addParameterOrderColumn(columnForOrders);
                addWorkPlanOrderColumn(columnForOrders);
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForOrders - have validation errors - "
                    + values.get(L_NAME.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForOrders(final Map<String, String> values) {
        final List<Entity> columnsForOrders = getColumnForOrdersDD().find()
                .add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER))).list().getEntities();

        for (Entity columnForOrder : columnsForOrders) {
            getColumnForOrdersDD().delete(columnForOrder.getId());
        }
    }

    private void addColumnForInputProducts(final Map<String, String> values) {
        Entity columnForInputProduct = getColumnForInputProductsDD().create();

        columnForInputProduct.setField(L_IDENTIFIER, values.get(L_IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        columnForInputProduct.setField(L_NAME, values.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        columnForInputProduct.setField(L_DESCRIPTION, values.get(L_DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        columnForInputProduct.setField(L_COLUMNFILLER, values.get(L_COLUMNFILLER.toLowerCase(Locale.ENGLISH)));
        columnForInputProduct.setField(L_ALIGNMENT, values.get(L_ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for input products item {column=" + columnForInputProduct.getStringField(L_NAME) + "}");
        }

        columnForInputProduct = columnForInputProduct.getDataDefinition().save(columnForInputProduct);

        if (columnForInputProduct.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for input products saved {column=" + columnForInputProduct.toString() + "}");
            }

            if (L_TRUE.equals(values.get(L_ACTIVE))) {
                addParameterInputColumn(columnForInputProduct);
                addOperationInputColumn(columnForInputProduct);
                addTechnologyOperationInputColumn(columnForInputProduct);
                addOrderOperationInputColumn(columnForInputProduct);
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForInputProducts - have validation errors - "
                    + values.get(L_NAME.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForInputProducts(final Map<String, String> values) {
        final List<Entity> columnsForInputProduct = getColumnForInputProductsDD().find()
                .add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER))).list().getEntities();

        for (Entity columnForInputProduct : columnsForInputProduct) {
            getColumnForInputProductsDD().delete(columnForInputProduct.getId());
        }
    }

    private void addColumnForOutputProducts(final Map<String, String> values) {
        Entity columnForOutputProduct = getColumnForOutputProductsDD().create();

        columnForOutputProduct.setField(L_IDENTIFIER, values.get(L_IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        columnForOutputProduct.setField(L_NAME, values.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        columnForOutputProduct.setField(L_DESCRIPTION, values.get(L_DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        columnForOutputProduct.setField(L_COLUMNFILLER, values.get(L_COLUMNFILLER.toLowerCase(Locale.ENGLISH)));
        columnForOutputProduct.setField(L_ALIGNMENT, values.get(L_ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for output products item {column=" + columnForOutputProduct.getStringField(L_NAME) + "}");
        }

        columnForOutputProduct = columnForOutputProduct.getDataDefinition().save(columnForOutputProduct);

        if (columnForOutputProduct.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for output products saved {column=" + columnForOutputProduct.toString() + "}");
            }

            if (L_TRUE.equals(values.get(L_ACTIVE))) {
                addParameterOutputColumn(columnForOutputProduct);
                addOperationOutputColumn(columnForOutputProduct);
                addTechnologyOperationOutputColumn(columnForOutputProduct);
                addOrderOperationOutputColumn(columnForOutputProduct);
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForOutputProducts - have validation errors - "
                    + values.get(L_NAME.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForOutputProducts(final Map<String, String> values) {
        final List<Entity> columnsForOutputProduct = getColumnForOutputProductsDD().find()
                .add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER))).list().getEntities();

        for (Entity columnForOutputProduct : columnsForOutputProduct) {
            getColumnForOutputProductsDD().delete(columnForOutputProduct.getId());
        }
    }

    private void addParameterOrderColumn(final Entity columnForOrders) {
        Entity parameterOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_ORDER_COLUMN).create();
        parameterOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);
        parameterOrderColumn.setField("parameter", parameterService.getParameter());
        parameterOrderColumn = parameterOrderColumn.getDataDefinition().save(parameterOrderColumn);

        if (parameterOrderColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter order columns {column=" + parameterOrderColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrderColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - workPlanOrderColumn - have validation errors - "
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
            throw new IllegalStateException("Saved entity - parameterInputColumn - have validation errors - "
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
            throw new IllegalStateException("Saved entity - parameterOutputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - operationInputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - operationOutputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - technologyOperationInputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - technologyOperationOutputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - orderOperationInputColumn - have validation errors - "
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
                throw new IllegalStateException("Saved entity - orderOperationOutputColumn - have validation errors - "
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

    private DataDefinition getColumnForOrdersDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS);
    }

    private DataDefinition getColumnForInputProductsDD() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS);
    }

    private DataDefinition getColumnForOutputProductsDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS);
    }

    public boolean databaseHasToBePrepared() {
        return checkIfColumnForInputProductsIsEmpty() && checkIfColumnForOutputProductsIsEmpty();
    }

    private boolean checkIfColumnForInputProductsIsEmpty() {
        return getColumnForInputProductsDD().find().list().getTotalNumberOfEntities() == 0;
    }

    private boolean checkIfColumnForOutputProductsIsEmpty() {
        return getColumnForOutputProductsDD().find().list().getTotalNumberOfEntities() == 0;
    }

    private InputStream getXmlFile(final String plugin, final String type) throws IOException {
        return WorkPlansColumnLoaderServiceImpl.class.getResourceAsStream("/" + plugin + "/model/data/" + type + ".xml");
    }
}
