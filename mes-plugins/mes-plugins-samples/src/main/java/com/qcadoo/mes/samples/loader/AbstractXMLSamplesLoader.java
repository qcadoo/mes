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
package com.qcadoo.mes.samples.loader;

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
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractXMLSamplesLoader extends AbstractSamplesLoader {

    /**
     * Get parsed samples data set from one XML file and prepare database. See
     * {@link MinimalSamplesLoader#readData(Map, String, Element)} for practical example.
     * 
     * @param values
     * @param type
     * @param node
     */
    protected abstract void readData(final Map<String, String> values, final String type, final Element node);

    protected void readDataFromXML(final String dataset, final String type, final String locale) {

        LOG.info("Loading test data from " + type + "_" + locale + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(dataset, type, locale));
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");
            for (Element node : nodes) {

                @SuppressWarnings("unchecked")
                List<Attribute> attributes = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();
                for (Attribute attribute : attributes) {
                    values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
                }
                readData(values, type, node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private InputStream getXmlFile(final String dataset, final String object, final String locale) throws IOException {
        return TestSamplesLoader.class.getResourceAsStream("/samples/" + dataset + "/" + object + "_" + locale + ".xml");
    }

}
