/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.basic.constants.CountryFields.CODE;
import static com.qcadoo.mes.basic.constants.CountryFields.COUNTRY;

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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class CountryLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CountryLoader.class);

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void loadCountries() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Country table will be populated ...");
            }
            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading data from country" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getContryXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");

            for (Element node : nodes) {
                parseAndAddCountry(node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void parseAndAddCountry(final Element node) {
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = node.getAttributes();
        Map<String, String> values = new HashMap<String, String>();

        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
        }

        addCountry(values);
    }

    private void addCountry(final Map<String, String> values) {
        DataDefinition conutryDataDefinition = getCountryDataDefinition();
        Entity country = conutryDataDefinition.create();

        country.setField(CODE, values.get(CODE.toLowerCase(Locale.ENGLISH)));
        country.setField(COUNTRY, values.get(COUNTRY.toLowerCase(Locale.ENGLISH)));

        country = conutryDataDefinition.save(country);

        if (country.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Country saved {country : " + country.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved country entity have validation errors - "
                    + values.get(COUNTRY.toLowerCase(Locale.ENGLISH)));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getCountryDataDefinition().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getCountryDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COUNTRY);
    }

    private InputStream getContryXmlFile() throws IOException {
        return CountryLoader.class.getResourceAsStream("/basic/model/data/country" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }

}
