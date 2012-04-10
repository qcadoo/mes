/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

    public void setParameterDefaultValues() {
        Entity parameter = getParameter();
        if (parameter != null) {
            for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                    continue;
                }

                parameter.setField(workPlanParameter, false);
            }

            if (parameter.isValid()) {
                parameter.getDataDefinition().save(parameter);
            }
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

                if (operation.isValid()) {
                    operation.getDataDefinition().save(operation);
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

                if (technologyOperationComponent.isValid()) {
                    technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
                }
            }
        }

    }

    public void setOrderOperationComponentDefaultValues() {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity orderOperationComponent : orderOperationComponents) {
                for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                    if (workPlanParameter.equals(WorkPlansConstants.IMAGE_URL_IN_WORK_PLAN_FIELD)) {
                        continue;
                    }

                    orderOperationComponent.setField(workPlanParameter, false);
                }

                if (orderOperationComponent.isValid()) {
                    orderOperationComponent.getDataDefinition().save(orderOperationComponent);
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

        if (columnForOrders.isValid()) {
            columnForOrders = columnForOrders.getDataDefinition().save(columnForOrders);

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
        Entity columnForOrders = getColumnForOrdersDD().find().add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER)))
                .uniqueResult();

        if (columnForOrders != null) {
            getColumnForOrdersDD().delete(columnForOrders.getId());
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

        if (columnForInputProduct.isValid()) {
            columnForInputProduct = columnForInputProduct.getDataDefinition().save(columnForInputProduct);

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
        Entity columnForInputProduct = getColumnForInputProductsDD().find()
                .add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER))).uniqueResult();

        if (columnForInputProduct != null) {
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

        if (columnForOutputProduct.isValid()) {
            columnForOutputProduct = columnForOutputProduct.getDataDefinition().save(columnForOutputProduct);

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
        Entity columnForOutputProduct = getColumnForOutputProductsDD().find()
                .add(SearchRestrictions.eq(L_IDENTIFIER, values.get(L_IDENTIFIER))).uniqueResult();

        if (columnForOutputProduct != null) {
            getColumnForOutputProductsDD().delete(columnForOutputProduct.getId());
        }
    }

    private void addParameterOrderColumn(final Entity columnForOrders) {
        Entity parameter = getParameter();

        if (parameter != null) {
            Entity parameterOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_ORDER_COLUMN).create();

            parameterOrderColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

            if (parameterOrderColumn.isValid()) {
                parameterOrderColumn = parameterOrderColumn.getDataDefinition().save(parameterOrderColumn);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to parameter order columns {column=" + parameterOrderColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - parameterOrderColumn - have validation errors - "
                        + columnForOrders.toString());
            }
        }
    }

    private void addWorkPlanOrderColumn(final Entity columnForOrders) {
        List<Entity> workPlans = getWorkPlans();

        if (workPlans != null) {
            for (Entity workPlan : workPlans) {
                Entity workPlanOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_WORK_PLAN_ORDER_COLUMN).create();

                workPlanOrderColumn.setField(WorkPlansConstants.MODEL_WORK_PLAN, workPlan);
                workPlanOrderColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);

                if (workPlanOrderColumn.isValid()) {
                    workPlanOrderColumn = workPlanOrderColumn.getDataDefinition().save(workPlanOrderColumn);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to work plan order columns {column=" + workPlanOrderColumn.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - workPlanOrderColumn - have validation errors - "
                            + columnForOrders.toString());
                }
            }
        }
    }

    private void addParameterInputColumn(final Entity columnForInputProducts) {
        Entity parameter = getParameter();

        if (parameter != null) {
            Entity parameterInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_INPUT_COLUMN).create();

            parameterInputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            if (parameterInputColumn.isValid()) {
                parameterInputColumn = parameterInputColumn.getDataDefinition().save(parameterInputColumn);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to parameter input columns {column=" + parameterInputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - parameterInputColumn - have validation errors - "
                        + columnForInputProducts.toString());
            }
        }
    }

    private void addParameterOutputColumn(final Entity columnForOutputProducts) {
        Entity parameter = getParameter();

        if (parameter != null) {
            Entity parameterOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COLUMN).create();

            parameterOutputColumn.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            if (parameterOutputColumn.isValid()) {
                parameterOutputColumn = parameterOutputColumn.getDataDefinition().save(parameterOutputColumn);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to parameter output columns {column=" + parameterOutputColumn.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - parameterOutputColumn - have validation errors - "
                        + columnForOutputProducts.toString());
            }
        }
    }

    private void addOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                Entity operationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_OPERATION_INPUT_COLUMN).create();

                operationInputColumn.setField(TechnologiesConstants.MODEL_OPERATION, operation);
                operationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

                if (operationInputColumn.isValid()) {
                    operationInputColumn = operationInputColumn.getDataDefinition().save(operationInputColumn);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to operation input columns {column=" + operationInputColumn.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - operationInputColumn - have validation errors - "
                            + columnForInputProducts.toString());
                }
            }
        }
    }

    private void addOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                Entity operationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_OPERATION_OUTPUT_COLUMN).create();

                operationOutputColumn.setField(TechnologiesConstants.MODEL_OPERATION, operation);
                operationOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

                if (operationOutputColumn.isValid()) {
                    operationOutputColumn = operationOutputColumn.getDataDefinition().save(operationOutputColumn);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to operation output columns {column=" + operationOutputColumn.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - operationOutputColumn - have validation errors - "
                            + columnForOutputProducts.toString());
                }
            }
        }
    }

    private void addTechnologyOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                Entity technologyOperationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_INPUT_COLUMN).create();

                technologyOperationInputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                technologyOperationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS,
                        columnForInputProducts);

                if (technologyOperationInputColumn.isValid()) {
                    technologyOperationInputColumn = technologyOperationInputColumn.getDataDefinition().save(
                            technologyOperationInputColumn);

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
    }

    private void addTechnologyOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                Entity technologyOperationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_OUTPUT_COLUMN).create();

                technologyOperationOutputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                technologyOperationOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS,
                        columnForOutputProducts);

                if (technologyOperationOutputColumn.isValid()) {
                    technologyOperationOutputColumn = technologyOperationOutputColumn.getDataDefinition().save(
                            technologyOperationOutputColumn);

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
    }

    private void addOrderOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity technologyInstanceOperationComponent : orderOperationComponents) {
                Entity orderOperationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COLUMN).create();

                orderOperationInputColumn.setField(TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        technologyInstanceOperationComponent);
                orderOperationInputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

                if (orderOperationInputColumn.isValid()) {
                    orderOperationInputColumn = orderOperationInputColumn.getDataDefinition().save(orderOperationInputColumn);

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
    }

    private void addOrderOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity technologyInstanceOperationComponent : orderOperationComponents) {
                Entity orderOperationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COLUMN).create();

                orderOperationOutputColumn.setField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        technologyInstanceOperationComponent);
                orderOperationOutputColumn.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

                if (orderOperationOutputColumn.isValid()) {
                    orderOperationOutputColumn = orderOperationOutputColumn.getDataDefinition().save(orderOperationOutputColumn);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to order operation output columns {column="
                                + orderOperationOutputColumn.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - orderOperationOutputColumn - have validation errors - "
                            + columnForOutputProducts.toString());
                }
            }
        }
    }

    private Entity getParameter() {
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                .uniqueResult();

        if (parameter == null) {
            return null;
        } else {
            return parameter;
        }
    }

    private List<Entity> getWorkPlans() {
        List<Entity> workPlans = dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN).find().list().getEntities();

        if (workPlans == null) {
            return null;
        } else {
            return workPlans;
        }
    }

    private List<Entity> getOperations() {
        List<Entity> operations = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION).find().list().getEntities();

        if (operations == null) {
            return null;
        } else {
            return operations;
        }
    }

    private List<Entity> getTechnologyOperationComponents() {
        List<Entity> technologyOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .list().getEntities();

        if (technologyOperationComponents == null) {
            return null;
        } else {
            return technologyOperationComponents;
        }
    }

    private List<Entity> getOrderOperationComponents() {
        List<Entity> orderOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .find().list().getEntities();

        if (orderOperationComponents == null) {
            return null;
        } else {
            return orderOperationComponents;
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
