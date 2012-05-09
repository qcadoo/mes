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

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_CURRENCY;

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
import com.qcadoo.model.api.DataDefinition;
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
    public final void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Currency table will be populated...");
            }
            readDataFromXML();
        }

    }

    private void readDataFromXML() {
        LOG.info("Loading test data from currency.xml ...");

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
        Map<String, String> values = new HashMap<String, String>();
        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
        }
        addCurrency(values);
    }

    private void addCurrency(final Map<String, String> values) {
        DataDefinition currencyDataDefinition = getCurrencyDataDefinition();
        Entity currency = currencyDataDefinition.create();

        currency.setField(L_CURRENCY, values.get(L_CURRENCY.toLowerCase(Locale.ENGLISH)));
        currency.setField(L_ALPHABETIC_CODE, values.get(L_ALPHABETIC_CODE.toLowerCase(Locale.ENGLISH)));
        currency.setField(L_ISO_CODE, Integer.valueOf(values.get(L_ISO_CODE.toLowerCase(Locale.ENGLISH))));
        currency.setField(L_MINOR_UNIT, Integer.valueOf(values.get(L_MINOR_UNIT.toLowerCase(Locale.ENGLISH))));

        currency = currencyDataDefinition.save(currency);
        if (currency.isValid() && LOG.isDebugEnabled()) {
            LOG.debug("Currency saved {currency=" + currency.toString() + "}");
        } else {
            throw new IllegalStateException("Saved currency entity have validation errors - " + values.get("CURRENCY"));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getCurrencyDataDefinition().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getCurrencyDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_CURRENCY);
    }

    private InputStream getCurrencyXmlFile() throws IOException {
        return CurrencyLoaderModule.class.getResourceAsStream("/basic/model/data/currency.xml");
    }
}
