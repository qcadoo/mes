/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.cmmsMachineParts;

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

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class DefaultFaultTypesLoaderModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultFaultTypesLoaderModule.class);

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DataDefinitionService dataDefinitionService;
 
    @Override
    public void enable() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Fault types table will be populated ...");
            }
            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading data from defaultFaultTypes" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage()
                + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getFaultTypesXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");

            for (Element node : nodes) {
                parseAndAddFaultType(node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void parseAndAddFaultType(final Element node) {
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = node.getAttributes();
        Map<String, String> values = new HashMap<String, String>();

        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.ENGLISH), attribute.getValue());
        }

        addFaultType(values);
    }

    private void addFaultType(final Map<String, String> values) {
        DataDefinition faultTypeDataDefinition = getFaultTypeDataDefinition();
        Entity faultType = faultTypeDataDefinition.create();

        faultType.setField(FaultTypeFields.NAME, values.get(FaultTypeFields.NAME.toLowerCase(Locale.ENGLISH)));
        faultType.setField(FaultTypeFields.APPLIES_TO, values.get(FaultTypeFields.APPLIES_TO.toLowerCase(Locale.ENGLISH)));

        faultType = faultTypeDataDefinition.save(faultType);

        if (faultType.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Fault type saved {faultType : " + faultType.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved fault type entity have validation errors - "
                    + values.get(FaultTypeFields.NAME.toLowerCase(Locale.ENGLISH)));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getFaultTypeDataDefinition().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getFaultTypeDataDefinition() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_FAULT_TYPE);
    }

    private InputStream getFaultTypesXmlFile() throws IOException {
        return DefaultFaultTypesLoaderModule.class.getResourceAsStream("/cmmsMachineParts/model/data/defaultFaultTypes" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }
}
