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
package com.qcadoo.mes.basic;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class CurrencyLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyLoader.class);

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void loadCurrencies() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Currency table will be populated ...");
            }

            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading data from currency.xml" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(getCurrencyXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");

            for (Element node : nodes) {
                parseAndAddCurrency(node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void parseAndAddCurrency(final Element node) {
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = node.getAttributes();

        Map<String, String> values = Maps.newHashMap();

        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
        }

        addCurrency(values);
    }

    private void addCurrency(final Map<String, String> values) {
        DataDefinition currencyDD = getCurrencyDD();

        Entity currency = currencyDD.create();

        currency.setField(CurrencyFields.CURRENCY, values.get(CurrencyFields.CURRENCY.toLowerCase(Locale.ENGLISH)));
        currency.setField(CurrencyFields.ALPHABETIC_CODE, values.get(CurrencyFields.ALPHABETIC_CODE.toLowerCase(Locale.ENGLISH)));
        currency.setField(CurrencyFields.ISO_CODE,
                Integer.valueOf(values.get(CurrencyFields.ISO_CODE.toLowerCase(Locale.ENGLISH))));
        currency.setField(CurrencyFields.MINOR_UNIT,
                Integer.valueOf(values.get(CurrencyFields.MINOR_UNIT.toLowerCase(Locale.ENGLISH))));
        currency.setField(CurrencyFields.EXCHANGE_RATE, BigDecimal.ONE);

        currency = currencyDD.save(currency);

        if (currency.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Currency saved {currency : " + currency.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved currency entity have validation errors - "
                    + values.get(CurrencyFields.CURRENCY.toLowerCase(Locale.ENGLISH)));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getCurrencyDD().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getCurrencyDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
    }

    private InputStream getCurrencyXmlFile() throws IOException {
        return CurrencyLoader.class.getResourceAsStream("/basic/model/data/currency.xml");
    }

}
