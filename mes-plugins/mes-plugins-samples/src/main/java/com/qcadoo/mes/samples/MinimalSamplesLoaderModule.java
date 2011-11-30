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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Component
public class MinimalSamplesLoaderModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(MinimalSamplesLoaderModule.class);

    @Autowired
    private SecurityRolesService securityRolesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Value("${samplesDatasetLocale}")
    private String locale;

    private static final String BASIC_PLUGIN_IDENTIFIER = "basic";

    private static final String BASIC_MODEL_COMPANY = "company";

    private static final String BASIC_MODEL_PARAMETER = "parameter";

    private static final String BASIC_MODEL_CURRENCY = "currency";

    private static final String BASIC_MODEL_SHIFT = "shift";

    @Override
    @Transactional
    public void multiTenantEnable() {
        checkLocale();

        if (isEnabled("productionCounting")) {
            setParameters();
        }
        if (isEnabled(BASIC_PLUGIN_IDENTIFIER)) {
            readDataFromXML("units");
            readDataFromXML("shifts");
            readDataFromXML("users");
            readDataFromXML("company");
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
        return TestSamplesLoaderModule.class.getResourceAsStream("/com/qcadoo/mes/samples/minimal/" + type + "_" + locale
                + ".xml");
    }

    private void readDataFromXML(final String type) {

        LOG.info("Loading test data from " + type + "_" + locale + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(type));
            Element rootNode = document.getRootElement();
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {

                Element node = list.get(i);
                List<Attribute> listOfAtribute = node.getAttributes();
                readData(listOfAtribute, type, node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    private void readData(final List<Attribute> attributes, final String type, final Element node) {
        Map<String, String> values = new HashMap<String, String>();

        for (int i = 0; i < attributes.size(); i++) {
            values.put(attributes.get(i).getName().toLowerCase(), attributes.get(i).getValue());
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
        Entity company = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_COMPANY).create();

        company.setField("number", values.get("number"));
        company.setField("companyFullName", values.get("companyfullname"));
        company.setField("tax", values.get("tax"));
        company.setField("street", values.get("street"));
        company.setField("house", values.get("house"));
        company.setField("flat", values.get("flat"));
        company.setField("zipCode", values.get("zipcode"));
        company.setField("city", values.get("city"));
        company.setField("state", values.get("state"));
        company.setField("country", values.get("country"));
        company.setField("email", values.get("email"));
        company.setField("addressWww", values.get("addresswww"));
        company.setField("phone", values.get("phone"));
        company.setField("owner", values.get("owner"));

        company = company.getDataDefinition().save(company);

        validateEntity(company);
    }

    private void setParameters() {
        Entity params = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_PARAMETER).create();

        String alphabeticCode = "";

        if ("pl".equals(locale)) {
            alphabeticCode = "PLN";
        } else {
            alphabeticCode = "USD";
        }

        Entity currency = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_CURRENCY).find()
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
        Entity shift = dataDefinitionService.get(BASIC_PLUGIN_IDENTIFIER, BASIC_MODEL_SHIFT).create();

        shift.setField("name", values.get("name"));
        shift.setField("mondayWorking", values.get("mondayworking"));
        shift.setField("mondayHours", values.get("mondayhours"));
        shift.setField("tuesdayWorking", values.get("tuesdayworking"));
        shift.setField("tuesdayHours", values.get("tuesdayhours"));
        shift.setField("wensdayWorking", values.get("wensdayworking"));
        shift.setField("wensdayHours", values.get("wensdayhours"));
        shift.setField("thursdayWorking", values.get("thursdayworking"));
        shift.setField("thursdayHours", values.get("thursdayhours"));
        shift.setField("fridayWorking", values.get("fridayworking"));
        shift.setField("fridayHours", values.get("fridayhours"));
        shift.setField("saturdayWorking", values.get("saturdayworking"));
        shift.setField("saturdayHours", values.get("saturdayhours"));
        shift.setField("sundayWorking", values.get("sundayworking"));
        shift.setField("sundayHours", values.get("sundayhours"));

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
