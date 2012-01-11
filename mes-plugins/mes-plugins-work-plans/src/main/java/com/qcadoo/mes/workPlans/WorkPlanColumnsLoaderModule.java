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
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.plugin.api.Module;

@Component
public class WorkPlanColumnsLoaderModule extends Module {

    private static final String COLUMNS_FOR_INPUT_PRODUCTS = "columnsForInputProducts";

    private static final String COLUMNS_FOR_OUTPUT_PRODUCTS = "columnsForOutputProducts";

    private static final String PARAMETER_INPUT_COMPONENT = "parameterInputComponent";

    private static final String PARAMETER_OUTPUT_COMPONENT = "parameterOutputComponent";

    private static final String NAME_FIELD = "name";

    private static final String DESCRIPTION_FIELD = "description";

    private static final String PLUGINIDENTIFIER_FIELD = "pluginIdentifier";

    private static final String[] COLUMN_ATTRIBUTES = new String[] { "NAME", "DESCRIPTION", "PLUGINIDENTIFIER" };

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
        readDataFromXML(COLUMNS_FOR_INPUT_PRODUCTS, COLUMN_ATTRIBUTES);
        readDataFromXML(COLUMNS_FOR_OUTPUT_PRODUCTS, COLUMN_ATTRIBUTES);
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

        if (COLUMNS_FOR_INPUT_PRODUCTS.equals(type)) {
            addColumnForInputProducts(values);
        }

        if (COLUMNS_FOR_OUTPUT_PRODUCTS.equals(type)) {
            addColumnForOutputProducts(values);
        }
    }

    private void addColumnForInputProducts(final Map<String, String> values) {
        Entity column = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS).create();

        column.setField(NAME_FIELD, values.get("NAME"));
        column.setField(DESCRIPTION_FIELD, values.get("DESCRIPTION"));
        column.setField(PLUGINIDENTIFIER_FIELD, values.get("PLUGINIDENTIFIER"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test column item {column=" + column.getStringField(NAME_FIELD) + "}");
        }

        if (column.isValid()) {
            column = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS).save(column);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column saved {column=" + column.toString() + "}");
            }

            Entity parameter = getParameter();

            Entity parameterInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_INPUT_COMPONENT).create();

            parameterInputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterInputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS, column);

            if (parameterInputComponent.isValid()) {
                parameterInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_PARAMETER_INPUT_COMPONENT).save(parameterInputComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to input products columns {column=" + parameterInputComponent.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity have validation errors");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + values.get("NAME"));
        }
    }

    private void addColumnForOutputProducts(final Map<String, String> values) {
        Entity columnForOutputProducts = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS).create();

        columnForOutputProducts.setField(NAME_FIELD, values.get("NAME"));
        columnForOutputProducts.setField(DESCRIPTION_FIELD, values.get("DESCRIPTION"));
        columnForOutputProducts.setField(PLUGINIDENTIFIER_FIELD, values.get("PLUGINIDENTIFIER"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test column item {column=" + columnForOutputProducts.getStringField(NAME_FIELD) + "}");
        }

        if (columnForOutputProducts.isValid()) {
            columnForOutputProducts = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS).save(columnForOutputProducts);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Column saved {column=" + columnForOutputProducts.toString() + "}");
            }

            Entity parameter = getParameter();

            Entity parameterOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COMPONENT).create();

            parameterOutputComponent.setField(BasicConstants.MODEL_PARAMETER, parameter);
            parameterOutputComponent.setField(WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            if (parameterOutputComponent.isValid()) {
                parameterOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COMPONENT).save(parameterOutputComponent);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Column added to output products columns {column=" + parameterOutputComponent.toString() + "}");
                }
            } else {
                throw new IllegalStateException("Saved entity have validation errors");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + values.get("NAME"));
        }
    }

    private Entity getParameter() {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();

        Entity parameter = null;

        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if (parameter == null) {
            return null;
        } else {
            return parameter;
        }
    }

    private boolean databaseHasToBePrepared() {
        return checkIfColumnForInputProductsAreEmpty() && checkIfColumnForOutputProductsAreEmpty();
    }

    private boolean checkIfColumnForInputProductsAreEmpty() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS).find().list()
                .getTotalNumberOfEntities() == 0;
    }

    private boolean checkIfColumnForOutputProductsAreEmpty() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS).find().list()
                .getTotalNumberOfEntities() == 0;
    }
}
