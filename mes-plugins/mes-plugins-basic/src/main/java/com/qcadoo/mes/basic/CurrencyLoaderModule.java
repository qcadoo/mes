/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;

@Component
public class CurrencyLoaderModule extends Module {

    private static final String L_CURRENCY = "currency";

    private static final String L_ALPHABETIC_CODE = "alphabeticCode";

    private static final String L_ISO_CODE = "isoCode";

    private static final String L_MINOR_UNIT = "minorUnit";

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
        readDataFromXML(L_CURRENCY);

    }

    private void readDataFromXML(final String type) {
        LOG.info("Loading test data from " + type + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(type));
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
                readData(type, values);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String type, final Map<String, String> values) {
        if (L_CURRENCY.equals(type)) {
            addCurrency(values);
        }
    }

    private void addCurrency(final Map<String, String> values) {
        Entity currency = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).create();

        currency.setField(L_CURRENCY, values.get(L_CURRENCY.toLowerCase(Locale.ENGLISH)));
        currency.setField(L_ALPHABETIC_CODE, values.get(L_ALPHABETIC_CODE.toLowerCase(Locale.ENGLISH)));
        currency.setField(L_ISO_CODE, Integer.valueOf(values.get(L_ISO_CODE.toLowerCase(Locale.ENGLISH))));
        currency.setField(L_MINOR_UNIT, Integer.valueOf(values.get(L_MINOR_UNIT.toLowerCase(Locale.ENGLISH))));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test currency item {currency=" + currency.getStringField(L_CURRENCY) + "}");
        }

        currency = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).save(currency);
        if (currency.isValid()) {
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

    private InputStream getXmlFile(final String type) throws IOException {
        return CurrencyLoaderModule.class.getResourceAsStream("/basic/model/data/" + type + ".xml");
    }
}
