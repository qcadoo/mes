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

import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class DefaultActionsLoaderModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultActionsLoaderModule.class);

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void enable() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Actions table will be populated ...");
            }
            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading data from defaultActions" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getActionsXmlFile());
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

        addAction(values);
    }

    private void addAction(final Map<String, String> values) {
        DataDefinition actionDD = getActionDataDefinition();
        Entity action = actionDD.create();

        action.setField(ActionFields.NAME, values.get(ActionFields.NAME.toLowerCase(Locale.ENGLISH)));
        action.setField(ActionFields.APPLIES_TO, values.get(ActionFields.APPLIES_TO.toLowerCase(Locale.ENGLISH)));
        action.setField(ActionFields.IS_DEFAULT, values.get(ActionFields.IS_DEFAULT.toLowerCase(Locale.ENGLISH)));
        action = actionDD.save(action);

        if (action.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action saved {action : " + action.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved action entity have validation errors - "
                    + values.get(ActionFields.NAME.toLowerCase(Locale.ENGLISH)));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getActionDataDefinition().find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getActionDataDefinition() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_ACTION);
    }

    private InputStream getActionsXmlFile() throws IOException {
        return DefaultActionsLoaderModule.class.getResourceAsStream("/cmmsMachineParts/model/data/defaultActions" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }

}
