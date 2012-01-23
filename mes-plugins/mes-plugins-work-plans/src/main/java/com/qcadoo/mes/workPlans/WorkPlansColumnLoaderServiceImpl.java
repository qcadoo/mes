/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkPlansColumnLoaderServiceImpl implements WorkPlansColumnLoaderService {

    private static final String TRUE = "true";

    private static final String COLUMN_FOR_PRODUCTS = "columnForProducts";

    private static final String IDENTIFIER_FIELD = "identifier";

    private static final String NAME_FIELD = "name";

    private static final String DESCRIPTION_FIELD = "description";

    private static final String COLUMNFILLER_FIELD = "columnFiller";

    private static final String TYPE_FIELD = "type";

    private static final String ACTIVE_FIELD = "active";

    private static final String OUTPUT_TYPE = "output";

    private static final String INPUT_TYPE = "input";

    private static final String BOTH_TYPE = "both";

    private static final String ADD_OPERATION = "add";

    private static final String DELETE_OPERATION = "delete";

    private static final String[] COLUMN_ATTRIBUTES = new String[] { IDENTIFIER_FIELD, NAME_FIELD, DESCRIPTION_FIELD,
            COLUMNFILLER_FIELD, TYPE_FIELD, ACTIVE_FIELD };

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlansColumnLoaderServiceImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setParameterDefaultValues() {
        Entity parameter = getParameter();
        if (parameter != null) {
            parameter.setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
            parameter.setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
            parameter.setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
            parameter.setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
            parameter.setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
            parameter.getDataDefinition().save(parameter);
        }
    }

    public void setOperationDefaultValues() {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                operation.setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
                operation.setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
                operation.setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
                operation.setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                operation.setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                operation.getDataDefinition().save(operation);
            }
        }
    }

    public void setTechnologyOperationComponentDefaultValues() {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                technologyOperationComponent.setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
                technologyOperationComponent.setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
                technologyOperationComponent.setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
                technologyOperationComponent.setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                technologyOperationComponent.setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
            }
        }

    }

    public void setOrderOperationComponentDefaultValues() {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity orderOperationComponent : orderOperationComponents) {
                orderOperationComponent.setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
                orderOperationComponent.setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
                orderOperationComponent.setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
                orderOperationComponent.setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                orderOperationComponent.setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
                orderOperationComponent.getDataDefinition().save(orderOperationComponent);
            }
        }
    }

    public void fillColumnsForProducts(final String plugin) {
        readDataFromXML(plugin, COLUMN_FOR_PRODUCTS, COLUMN_ATTRIBUTES, ADD_OPERATION);
    }

    public void clearColumnsForProducts(final String plugin) {
        readDataFromXML(plugin, COLUMN_FOR_PRODUCTS, COLUMN_ATTRIBUTES, DELETE_OPERATION);
    }

    private void readDataFromXML(final String plugin, final String type, final String[] attributes, final String operation) {
        LOG.info("Loading test data from " + type + ".xml ...");

        try {
            DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docBuildFactory.newDocumentBuilder();

            InputStream file = WorkPlansColumnLoaderServiceImpl.class.getResourceAsStream("/" + plugin + "/model/data/" + type
                    + ".xml");

            Document doc = docBuild.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("row");
            for (int node = 0; node < nodeList.getLength(); node++) {
                readData(type, attributes, nodeList, node, operation);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String type, final String[] attributes, final NodeList nodeLst, final int row,
            final String operation) {
        Map<String, String> values = new HashMap<String, String>();
        Node fstNode = nodeLst.item(row);

        for (String attribute : attributes) {
            if (fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)) != null) {
                String value = fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)).getNodeValue();
                values.put(attribute, value);
            }
        }

        if (COLUMN_FOR_PRODUCTS.equals(type)) {
            if (ADD_OPERATION.equals(operation)) {
                if (BOTH_TYPE.equals(values.get(TYPE_FIELD))) {
                    addColumnForInputProducts(values);
                    addColumnForOutputProducts(values);
                } else if (INPUT_TYPE.equals(values.get(TYPE_FIELD))) {
                    addColumnForInputProducts(values);
                } else if (OUTPUT_TYPE.equals(values.get(TYPE_FIELD))) {
                    addColumnForOutputProducts(values);
                } else {
                    throw new IllegalStateException("Incorrect type - " + values.get(TYPE_FIELD));
                }
            } else if (DELETE_OPERATION.equals(operation)) {
                if (BOTH_TYPE.equals(values.get(TYPE_FIELD))) {
                    deleteColumnForInputProducts(values);
                    deleteColumnForOutputProducts(values);
                } else if (INPUT_TYPE.equals(values.get(TYPE_FIELD))) {
                    deleteColumnForInputProducts(values);
                } else if (OUTPUT_TYPE.equals(values.get(TYPE_FIELD))) {
                    deleteColumnForOutputProducts(values);
                } else {
                    throw new IllegalStateException("Incorrect type - " + values.get(TYPE_FIELD));
                }
            }
        }
    }

    private void addColumnForInputProducts(final Map<String, String> values) {
        Entity columnForInputProduct = getColumnForInputProductsDD().create();

        columnForInputProduct.setField(IDENTIFIER_FIELD, values.get(IDENTIFIER_FIELD));
        columnForInputProduct.setField(NAME_FIELD, values.get(NAME_FIELD));
        columnForInputProduct.setField(DESCRIPTION_FIELD, values.get(DESCRIPTION_FIELD));
        columnForInputProduct.setField(COLUMNFILLER_FIELD, values.get(COLUMNFILLER_FIELD));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for input products item {column=" + columnForInputProduct.getStringField(NAME_FIELD) + "}");
        }

        if (columnForInputProduct.isValid()) {
            columnForInputProduct = columnForInputProduct.getDataDefinition().save(columnForInputProduct);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for input products saved {column=" + columnForInputProduct.toString() + "}");
            }

            if (TRUE.equals(values.get(ACTIVE_FIELD))) {
                addParameterInputColumn(columnForInputProduct);
                addOperationInputColumn(columnForInputProduct);
                addTechnologyOperationInputColumn(columnForInputProduct);
                addOrderOperationInputColumn(columnForInputProduct);
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForInputProducts - have validation errors - "
                    + values.get(NAME_FIELD.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForInputProducts(final Map<String, String> values) {
        Entity columnForInputProduct = getColumnForInputProductsDD().find()
                .add(SearchRestrictions.eq(IDENTIFIER_FIELD, values.get(IDENTIFIER_FIELD))).uniqueResult();

        if (columnForInputProduct != null) {
            getColumnForInputProductsDD().delete(columnForInputProduct.getId());
        }
    }

    private void addColumnForOutputProducts(final Map<String, String> values) {
        Entity columnForOutputProduct = getColumnForOutputProductsDD().create();

        columnForOutputProduct.setField(IDENTIFIER_FIELD, values.get(IDENTIFIER_FIELD));
        columnForOutputProduct.setField(NAME_FIELD, values.get(NAME_FIELD));
        columnForOutputProduct.setField(DESCRIPTION_FIELD, values.get(DESCRIPTION_FIELD));
        columnForOutputProduct.setField(COLUMNFILLER_FIELD, values.get(COLUMNFILLER_FIELD));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for output products item {column=" + columnForOutputProduct.getStringField(NAME_FIELD) + "}");
        }

        if (columnForOutputProduct.isValid()) {
            columnForOutputProduct = columnForOutputProduct.getDataDefinition().save(columnForOutputProduct);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for output products saved {column=" + columnForOutputProduct.toString() + "}");
            }

            if (TRUE.equals(values.get(ACTIVE_FIELD))) {
                addParameterOutputColumn(columnForOutputProduct);
                addOperationOutputColumn(columnForOutputProduct);
                addTechnologyOperationOutputColumn(columnForOutputProduct);
                addOrderOperationOutputColumn(columnForOutputProduct);
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForOutputProducts - have validation errors - "
                    + values.get(NAME_FIELD.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForOutputProducts(final Map<String, String> values) {
        Entity columnForOutputProduct = getColumnForOutputProductsDD().find()
                .add(SearchRestrictions.eq(IDENTIFIER_FIELD, values.get(IDENTIFIER_FIELD))).uniqueResult();

        if (columnForOutputProduct != null) {
            getColumnForOutputProductsDD().delete(columnForOutputProduct.getId());
        }
    }

    private void addParameterInputColumn(final Entity columnForInputProducts) {
        Entity parameter = getParameter();

        if (parameter != null) {
            Entity parameterInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_INPUT_COLUMN).create();

            parameterInputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            if (parameterInputComponent.isValid()) {
                parameterInputComponent = parameterInputComponent.getDataDefinition().save(parameterInputComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to parameter input components {column=" + parameterInputComponent.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - parameterInputComponent - have validation errors - "
                        + columnForInputProducts.toString());
            }
        }
    }

    private void addParameterOutputColumn(final Entity columnForOutputProducts) {
        Entity parameter = getParameter();

        if (parameter != null) {
            Entity parameterOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COLUMN).create();

            parameterOutputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            if (parameterOutputComponent.isValid()) {
                parameterOutputComponent = parameterOutputComponent.getDataDefinition().save(parameterOutputComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to parameter output components {column=" + parameterOutputComponent.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity - parameterOutputComponent - have validation errors - "
                        + columnForOutputProducts.toString());
            }
        }
    }

    private void addOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                Entity operationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_OPERATION_INPUT_COLUMN).create();

                operationInputComponent.setField(TechnologiesConstants.MODEL_OPERATION, operation);
                operationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

                if (operationInputComponent.isValid()) {
                    operationInputComponent = operationInputComponent.getDataDefinition().save(operationInputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to operation input components {column=" + operationInputComponent.toString()
                                + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - operationInputComponent - have validation errors - "
                            + columnForInputProducts.toString());
                }
            }
        }
    }

    private void addOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> operations = getOperations();

        if (operations != null) {
            for (Entity operation : operations) {
                Entity operationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_OPERATION_OUTPUT_COLUMN).create();

                operationOutputComponent.setField(TechnologiesConstants.MODEL_OPERATION, operation);
                operationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

                if (operationOutputComponent.isValid()) {
                    operationOutputComponent = operationOutputComponent.getDataDefinition().save(operationOutputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to operation output components {column=" + operationOutputComponent.toString()
                                + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - operationOutputComponent - have validation errors - "
                            + columnForOutputProducts.toString());
                }
            }
        }
    }

    private void addTechnologyOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                Entity technologyOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_INPUT_COLUMN).create();

                technologyOperationInputComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                technologyOperationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS,
                        columnForInputProducts);

                if (technologyOperationInputComponent.isValid()) {
                    technologyOperationInputComponent = technologyOperationInputComponent.getDataDefinition().save(
                            technologyOperationInputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to technology operation input components {column="
                                + technologyOperationInputComponent.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException(
                            "Saved entity - technologyOperationInputComponent - have validation errors - "
                                    + columnForInputProducts.toString());
                }
            }
        }
    }

    private void addTechnologyOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> technologyOperationComponents = getTechnologyOperationComponents();

        if (technologyOperationComponents != null) {
            for (Entity technologyOperationComponent : technologyOperationComponents) {
                Entity technologyOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_OUTPUT_COLUMN).create();

                technologyOperationOutputComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                technologyOperationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS,
                        columnForOutputProducts);

                if (technologyOperationOutputComponent.isValid()) {
                    technologyOperationOutputComponent = technologyOperationOutputComponent.getDataDefinition().save(
                            technologyOperationOutputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to technology operation output components {column="
                                + technologyOperationOutputComponent.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException(
                            "Saved entity - technologyOperationOutputComponent - have validation errors - "
                                    + columnForOutputProducts.toString());
                }
            }
        }
    }

    private void addOrderOperationInputColumn(final Entity columnForInputProducts) {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity orderOperationComponent : orderOperationComponents) {
                Entity orderOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COLUMN).create();

                orderOperationInputComponent.setField(ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT,
                        orderOperationComponent);
                orderOperationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

                if (orderOperationInputComponent.isValid()) {
                    orderOperationInputComponent = orderOperationInputComponent.getDataDefinition().save(
                            orderOperationInputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to order operation input components {column="
                                + orderOperationInputComponent.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - orderOperationInputComponent - have validation errors - "
                            + columnForInputProducts.toString());
                }
            }
        }
    }

    private void addOrderOperationOutputColumn(final Entity columnForOutputProducts) {
        List<Entity> orderOperationComponents = getOrderOperationComponents();

        if (orderOperationComponents != null) {
            for (Entity orderOperationComponent : orderOperationComponents) {
                Entity orderOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COLUMN).create();

                orderOperationOutputComponent.setField(ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT,
                        orderOperationComponent);
                orderOperationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS,
                        columnForOutputProducts);

                if (orderOperationOutputComponent.isValid()) {
                    orderOperationOutputComponent = orderOperationOutputComponent.getDataDefinition().save(
                            orderOperationOutputComponent);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Column added to order operation output components {column="
                                + orderOperationOutputComponent.toString() + "}");
                    }
                } else {
                    throw new IllegalStateException("Saved entity - orderOperationOutputComponent - have validation errors - "
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
                .get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                        ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT).find().list().getEntities();

        if (orderOperationComponents == null) {
            return null;
        } else {
            return orderOperationComponents;
        }
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

}
