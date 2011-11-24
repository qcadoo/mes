/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.samples;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class SamplesMinimalDataset extends Module {

    private static final String[] UNITS_ATTRIBUTES = new String[] { "name", "description" };

    private static final String[] SHIFTS_ATTRIBUTES = new String[] { "name", "mondayWorking", "mondayHours", "tuesdayWorking",
            "tuesdayHours", "wensdayWorking", "wensdayHours", "thursdayWorking", "thursdayHours", "fridayWorking", "fridayHours",
            "saturdayWorking", "saturdayHours", "sundayWorking", "sundayHours" };

    private static final String[] USERS_ATTRIBUTES = new String[] { "login", "email", "firstname", "lastname", "role" };

    private static final String[] COMPANY_ATTRIBUTES = new String[] { "companyFullName", "tax", "street", "house", "flat",
            "zipCode", "city", "state", "country", "email", "addressWww", "phone" };

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Value("${loadTestDataLocale}")
    private String locale;

    @Override
    @Transactional
    public void multiTenantEnable() {
        checkLocale();

        if (isEnabled("productionCounting")) {
            setParameters();
        }
        if (isEnabled(BasicConstants.PLUGIN_IDENTIFIER)) {
            readDataFromXML("units", UNITS_ATTRIBUTES);
            readDataFromXML("shifts", SHIFTS_ATTRIBUTES);
            readDataFromXML("users", USERS_ATTRIBUTES);
            readDataFromXML("company", COMPANY_ATTRIBUTES);
        }

    }

    private void checkLocale() {
        if ((locale != null) || ("".equals(locale))) {
            locale = Locale.getDefault().toString().substring(0, 2);

            if (!"pl".equals(locale) && !"en".equals(locale)) {
                locale = Locale.ENGLISH.toString().substring(0, 2);
            }
        }
    }

    private InputStream getXmlFile(final String type) throws IOException {
        return SamplesLoaderModule.class.getResourceAsStream("/com/qcadoo/mes/samples/minimal/" + type + "_" + locale + ".xml");
    }

    private void readDataFromXML(final String type, final String[] attributes) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(getXmlFile(type));
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName("row");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                readData(attributes, type, nodeLst, s);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
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

        if ("units".equals(type)) {
            addUnit(values);
        } else if ("shifts".equals(type)) {
            addShift(values);
        } else if ("users".equals(type)) {
            addUser(values);
        } else if ("company".equals(type)) {
            addCompany(values);
        }
    }

    private void addCompany(final Map<String, String> values) {
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).create();

        company.setField("companyFullName", values.get("companyFullName"));
        company.setField("tax", values.get("tax"));
        company.setField("street", values.get("street"));
        company.setField("house", values.get("house"));
        company.setField("flat", values.get("flat"));
        company.setField("zipCode", values.get("zipCode"));
        company.setField("city", values.get("city"));
        company.setField("state", values.get("state"));
        company.setField("country", values.get("country"));
        company.setField("email", values.get("email"));
        company.setField("addressWww", values.get("addressWww"));
        company.setField("phone", values.get("phone"));

        company = company.getDataDefinition().save(company);

        validateEntity(company);
    }

    private void setParameters() {
        Entity params = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).create();

        String alphabeticCode = "";

        if ("pl".equals(locale)) {
            alphabeticCode = "PLN";
        } else {
            alphabeticCode = "USD";
        }

        Entity currency = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY).find()
                .add(SearchRestrictions.eq("alphabeticCode", alphabeticCode)).uniqueResult();

        params.setField("registerQuantityInProduct", true);
        params.setField("registerQuantityOutProduct", true);
        params.setField("registerProductionTime", true);
        params.setField("currency", currency);

        params = params.getDataDefinition().save(params);

        validateEntity(params);
    }

    private void addUser(final Map<String, String> values) {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();

        user.setField("userName", values.get("login"));

        SecurityRole role = securityRolesService.getRoleByIdentifier(values.get("role"));
        user.setField("role", role.getName());
        user.setField("password", values.get("login"));
        user.setField("passwordConfirmation", values.get("login"));
        user.setField("enabled", true);

        user = user.getDataDefinition().save(user);

        validateEntity(user);
    }

    private void addShift(final Map<String, String> values) {
        Entity shift = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).create();

        shift.setField("name", values.get("name"));
        shift.setField("mondayWorking", values.get("mondayWorking"));
        shift.setField("mondayHours", values.get("mondayHours"));
        shift.setField("tuesdayWorking", values.get("tuesdayWorking"));
        shift.setField("tuesdayHours", values.get("tuesdayHours"));
        shift.setField("wensdayWorking", values.get("wensdayWorking"));
        shift.setField("wensdayHours", values.get("wensdayHours"));
        shift.setField("thursdayWorking", values.get("thursdayWorking"));
        shift.setField("thursdayHours", values.get("thursdayHours"));
        shift.setField("fridayWorking", values.get("fridayWorking"));
        shift.setField("fridayHours", values.get("fridayHours"));
        shift.setField("saturdayWorking", values.get("saturdayWorking"));
        shift.setField("saturdayHours", values.get("saturdayHours"));
        shift.setField("sundayWorking", values.get("sundayWorking"));
        shift.setField("sundayHours", values.get("sundayHours"));

        shift = shift.getDataDefinition().save(shift);

        validateEntity(shift);
    }

    private void addUnit(final Map<String, String> values) {
        Entity dictionary = getDictionaryByName("units");

        Entity unit = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        unit.setField("dictionary", dictionary);
        unit.setField("name", values.get("name"));
        unit.setField("description", values.get("description"));

        unit = unit.getDataDefinition().save(unit);

        validateEntity(unit);
    }

    private boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }

    private Entity getDictionaryByName(final String dictionaryName) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq("name", dictionaryName))
                .setMaxResults(1).uniqueResult();
    }

    private void validateEntity(final Entity entity) {
        if (!entity.isValid()) {
            Map<String, ErrorMessage> errors = entity.getErrors();
            Set<String> keys = errors.keySet();
            StringBuilder stringError = new StringBuilder("Saved entity is invalid\n");
            for (String key : keys) {
                stringError.append("\t").append(key).append("  -  ").append(errors.get(key).getMessage()).append("\n");
            }
            throw new IllegalStateException(stringError.toString());
        }
    }
}
