/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;

@Component
public class WorkPlanColumnsLoaderModule extends Module {

    private static final String COLUMN_DEFINITION = "columnDefinition";

    private static final String IDENTIFIER_FIELD = "identifier";

    private static final String NAME_FIELD = "name";

    private static final String DESCRIPTION_FIELD = "description";

    private static final String PLUGINIDENTIFIER_FIELD = "pluginIdentifier";

    private static final String[] COLUMN_ATTRIBUTES = new String[] { "IDENTIFIER", "NAME", "DESCRIPTION", "PLUGINIDENTIFIER",
            "TYPE" };

    private static final Logger LOG = LoggerFactory.getLogger(WorkPlanColumnsLoaderModule.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public void multiTenantEnable() {
        if (!databaseHasToBePrepared()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Columns for input and output products table will be populated...");
        }

        setParameterDefaultValues();

        readDataFromXML(COLUMN_DEFINITION, COLUMN_ATTRIBUTES);
    }

    private void setParameterDefaultValues() {
        Entity parameters = getParameter();
        parameters.setField("hideDescriptionInWorkPlans", false);
        parameters.setField("hideDetailsInWorkPlans", false);
        parameters.setField("hideTechnologyAndOrderInWorkPlans", false);
        parameters.setField("dontPrintInputProductsInWorkPlans", false);
        parameters.setField("dontPrintOutputProductsInWorkPlans", false);
        parameters.getDataDefinition().save(parameters);
    }

    private void readDataFromXML(final String type, final String[] attributes) {
        LOG.info("Loading test data from " + type + ".xml ...");

        try {
            DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docBuildFactory.newDocumentBuilder();

            InputStream file = WorkPlanColumnsLoaderModule.class.getResourceAsStream("/workPlans/model/data/" + type + ".xml");

            Document doc = docBuild.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("row");
            for (int s = 0; s < nodeList.getLength(); s++) {
                readData(attributes, type, nodeList, s);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String[] attributes, final String type, final NodeList nodeLst, final int s) {
        Map<String, String> values = new HashMap<String, String>();
        Node fstNode = nodeLst.item(s);

        for (String attribute : attributes) {
            if (fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)) != null) {
                String value = fstNode.getAttributes().getNamedItem(attribute.toUpperCase(Locale.ENGLISH)).getNodeValue();
                values.put(attribute, value);
            }
        }

        if (COLUMN_DEFINITION.equals(type)) {
            addColumnDefinition(values);
        }
    }

    private void addColumnDefinition(final Map<String, String> values) {
        Entity columnDefinition = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_DEFINITION).create();

        columnDefinition.setField(IDENTIFIER_FIELD, values.get("IDENTIFIER"));
        columnDefinition.setField(NAME_FIELD, values.get("NAME"));
        columnDefinition.setField(DESCRIPTION_FIELD, values.get("DESCRIPTION"));
        columnDefinition.setField(PLUGINIDENTIFIER_FIELD, values.get("PLUGINIDENTIFIER"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test column item {column=" + columnDefinition.getStringField(NAME_FIELD) + "}");
        }

        if (columnDefinition.isValid()) {
            columnDefinition = columnDefinition.getDataDefinition().save(columnDefinition);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column saved {column=" + columnDefinition.toString() + "}");
            }

            if ("input".equals(values.get("TYPE"))) {
                addColumnForInputProducts(columnDefinition);
            } else if ("output".equals(values.get("TYPE"))) {
                addColumnForOutputProducts(columnDefinition);
            } else {
                addColumnForInputProducts(columnDefinition);
                addColumnForOutputProducts(columnDefinition);
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + values.get("NAME"));
        }
    }

    private void addColumnForInputProducts(final Entity columnDefinition) {
        Entity columnForInputProduct = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS).create();

        columnForInputProduct.setField(WorkPlansConstants.MODEL_COLUMN_DEFINITION, columnDefinition);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test column item {column="
                    + columnForInputProduct.getBelongsToField(WorkPlansConstants.MODEL_COLUMN_DEFINITION).getStringField(
                            NAME_FIELD) + "}");
        }

        if (columnForInputProduct.isValid()) {
            columnForInputProduct = columnForInputProduct.getDataDefinition().save(columnForInputProduct);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column saved {column=" + columnForInputProduct.toString() + "}");
            }

            addParameterInputComponent(columnForInputProduct);
            addOperationInputComponent(columnForInputProduct);
            addTechnologyOperationInputComponent(columnForInputProduct);
            addOrderOperationInputComponent(columnForInputProduct);
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnDefinition.toString());
        }
    }

    private void addColumnForOutputProducts(final Entity columnDefinition) {
        Entity columnForOutputProducts = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS).create();

        columnForOutputProducts.setField(WorkPlansConstants.MODEL_COLUMN_DEFINITION, columnDefinition);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test column item {column="
                    + columnForOutputProducts.getBelongsToField(WorkPlansConstants.MODEL_COLUMN_DEFINITION).getStringField(
                            NAME_FIELD) + "}");
        }

        if (columnForOutputProducts.isValid()) {
            columnForOutputProducts = columnForOutputProducts.getDataDefinition().save(columnForOutputProducts);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column saved {column=" + columnForOutputProducts.toString() + "}");
            }

            addParameterOutputComponent(columnForOutputProducts);
            addOperationOutputComponent(columnForOutputProducts);
            addTechnologyOperationOutputComponent(columnForOutputProducts);
            addOrderOperationOutputComponent(columnForOutputProducts);
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnDefinition.toString());
        }
    }

    private void addParameterInputComponent(final Entity columnForInputProducts) {
        Entity parameter = getParameter();

        Entity parameterInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_INPUT_COMPONENT).create();

        parameterInputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        if (parameterInputComponent.isValid()) {
            parameterInputComponent = parameterInputComponent.getDataDefinition().save(parameterInputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to input products columns {column=" + parameterInputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForInputProducts.toString());
        }
    }

    private void addParameterOutputComponent(final Entity columnForOutputProducts) {
        Entity parameter = getParameter();

        Entity parameterOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COMPONENT).create();

        parameterOutputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
        parameterOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

        if (parameterOutputComponent.isValid()) {
            parameterOutputComponent = parameterOutputComponent.getDataDefinition().save(parameterOutputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to output products columns {column=" + parameterOutputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForOutputProducts.toString());
        }
    }

    private void addOperationInputComponent(final Entity columnForInputProducts) {
        // TODO LUPO fix operations
        // EntityList operations = getOperations();

        Entity operationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_OPERATION_INPUT_COMPONENT).create();

        // operationInputComponent.setField(TechnologiesConstants.MODEL_OPERATION, operation);
        // operationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        if (operationInputComponent.isValid()) {
            // operationInputComponent = operationInputComponent.getDataDefinition().save(operationInputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to input products columns {column=" + operationInputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForInputProducts.toString());
        }
    }

    private void addOperationOutputComponent(final Entity columnForOutputProducts) {
        // TODO LUPO fix operations
        // EntityList operations = getOperations();

        Entity operationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_OPERATION_OUTPUT_COMPONENT).create();

        // operationOutputComponent.setField(TechnologiesConstants.MODEL_OPERATION, operation);
        // operationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

        if (operationOutputComponent.isValid()) {
            // operationOutputComponent = operationOutputComponent.getDataDefinition().save(operationOutputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to output products columns {column=" + operationOutputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForOutputProducts.toString());
        }
    }

    private void addTechnologyOperationInputComponent(final Entity columnForInputProducts) {
        // TODO LUPO fix technology operations
        // EntityList technologyOperationComponents = getTechnologyOperationComponents();

        Entity technologyOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_INPUT_COMPONENT).create();

        // technologyOperationInputComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
        // technologyOperationComponent);
        // technologyOperationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        if (technologyOperationInputComponent.isValid()) {
            // technologyOperationInputComponent =
            // technologyOperationInputComponent.getDataDefinition().save(technologyOperationInputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to input products columns {column=" + technologyOperationInputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForInputProducts.toString());
        }
    }

    private void addTechnologyOperationOutputComponent(final Entity columnForOutputProducts) {
        // TODO LUPO fix technology operations
        // EntityList technologyOperationComponents = getTechnologyOperationComponents();

        Entity technologyOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_OUTPUT_COMPONENT).create();

        // technologyOperationOutputComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
        // technologyOperationComponent);
        // technologyOperationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS,
        // columnForOutputProducts);

        if (technologyOperationOutputComponent.isValid()) {
            // technologyOperationOutputComponent = technologyOperationOutputComponent.getDataDefinition().save(
            // technologyOperationOutputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to output products columns {column=" + technologyOperationOutputComponent.toString()
                        + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForOutputProducts.toString());
        }
    }

    private void addOrderOperationInputComponent(final Entity columnForInputProducts) {
        // TODO LUPO fix order operations
        // EntityList orderOperationComponents = getOrderOperationComponents();

        Entity orderOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COMPONENT).create();

        // orderOperationInputComponent.setField(ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT,
        // orderOperationComponent);
        // orderOperationInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

        if (orderOperationInputComponent.isValid()) {
            // orderOperationInputComponent = orderOperationInputComponent.getDataDefinition().save(orderOperationInputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to input products columns {column=" + orderOperationInputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForInputProducts.toString());
        }
    }

    private void addOrderOperationOutputComponent(final Entity columnForOutputProducts) {
        // TODO LUPO fix order operations
        // EntityList orderOperationComponents = getOrderOperationComponents();

        Entity orderOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COMPONENT).create();

        // orderOperationOutputComponent.setField(ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT,
        // orderOperationComponent);
        // orderOperationOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

        if (orderOperationOutputComponent.isValid()) {
            // orderOperationOutputComponent =
            // orderOperationOutputComponent.getDataDefinition().save(orderOperationOutputComponent);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to output products columns {column=" + orderOperationOutputComponent.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + columnForOutputProducts.toString());
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

    private boolean databaseHasToBePrepared() {
        return checkIfColumnDefintnitionIsEmpty() && checkIfColumnForInputProductsIsEmpty()
                && checkIfColumnForOutputProductsIsEmpty();
    }

    private boolean checkIfColumnDefintnitionIsEmpty() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_DEFINITION).find()
                .list().getTotalNumberOfEntities() == 0;
    }

    private boolean checkIfColumnForInputProductsIsEmpty() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS).find().list()
                .getTotalNumberOfEntities() == 0;
    }

    private boolean checkIfColumnForOutputProductsIsEmpty() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS).find().list()
                .getTotalNumberOfEntities() == 0;
    }

}
