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
package com.qcadoo.mes.basic;

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
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;

@Component
public class CurrencyLoaderModule extends Module {

    private static final String MINOR_UNIT_FIELD = "minorUnit";

    private static final String ISO_CODE_FIELD = "isoCode";

    private static final String ALPHABETIC_CODE_FIELD = "alphabeticCode";

    private static final String CURRENCY_FIELD = "currency";

    private static final String[] CURRENCY_ATTRIBUTES = new String[] { "CURRENCY", "ALPHABETICCODE", "ISOCODE", "MINORUNIT" };

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyLoaderModule.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public void multiTenantEnable() {
        if (!databaseHasToBePrepared()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Currency table will be populated...");
        }
        readDataFromXML(CURRENCY_FIELD, CURRENCY_ATTRIBUTES);

    }

    private void readDataFromXML(final String type, final String[] attributes) {
        LOG.info("Loading test data from " + type + ".xml ...");

        try {
            DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docBuildFactory.newDocumentBuilder();

            InputStream file = CurrencyLoaderModule.class.getResourceAsStream("/basic/model/data/" + type + ".xml");

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

        if (CURRENCY_FIELD.equals(type)) {
            addCurrency(values);
        }
    }

    private void addCurrency(final Map<String, String> values) {
        Entity currency = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).create();

        currency.setField(CURRENCY_FIELD, values.get("CURRENCY"));
        currency.setField(ALPHABETIC_CODE_FIELD, values.get("ALPHABETICCODE"));
        currency.setField(ISO_CODE_FIELD, Integer.valueOf(values.get("ISOCODE")));
        currency.setField(MINOR_UNIT_FIELD, Integer.valueOf(values.get("MINORUNIT")));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test currency item {currency=" + currency.getStringField(CURRENCY_FIELD) + "}");
        }

        if (currency.isValid()) {
            currency = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).save(currency);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Currency saved {currency=" + currency.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity have validation errors - " + values.get("CURRENCY"));
        }
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).find().list()
                .getTotalNumberOfEntities() == 0;
    }
}
