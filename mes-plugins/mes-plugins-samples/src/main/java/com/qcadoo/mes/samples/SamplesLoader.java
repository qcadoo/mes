/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

public abstract class SamplesLoader {

    private static final String NAME = "name";

    private static final String EMAIL = "email";

    private static final Logger LOG = LoggerFactory.getLogger(SamplesLoader.class);

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityRolesService securityRolesService;

    abstract void loadData(final String dataset, final String locale);

    void readData(final Map<String, String> values, final String type, final Element node) {
        if ("activeCurrency".equals(type)) {
            addParameters(values);
        } else if ("dictionaries".equals(type)) {
            addDictionaryItems(values);
        } else if ("shifts".equals(type)) {
            addShifts(values);
        } else if ("company".equals(type)) {
            addCompany(values);
        }
    }

    void readDataFromXML(final String dataset, final String object, final String locale) {

        LOG.info("Loading test data from " + object + "_" + locale + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(dataset, object, locale));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {
                Element node = list.get(i);
                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtribute = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();

                for (int j = 0; j < listOfAtribute.size(); j++) {
                    values.put(listOfAtribute.get(j).getName().toLowerCase(), listOfAtribute.get(j).getValue());
                }
                readData(values, object, node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void addDictionaryItems(final Map<String, String> values) {
        Entity dictionary = getDictionaryByName(values.get(NAME));

        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        item.setField("dictionary", dictionary);
        item.setField(NAME, values.get("item"));
        item.setField("description", values.get("description"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test dictionary item {dictionary=" + dictionary.getField(NAME) + ", item=" + item.getField(NAME)
                    + ", description=" + item.getField("description") + "}");
        }

        item = item.getDataDefinition().save(item);
        validateEntity(item);
    }

    void addParameters(final Map<String, String> values) {
        LOG.info("Adding parameters");
        Entity params = dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER,
                SamplesConstants.BASIC_MODEL_PARAMETER).create();

        Entity currency = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_CURRENCY).find()
                .add(SearchRestrictions.eq("alphabeticCode", values.get("code"))).uniqueResult();

        params.setField("currency", currency);

        if (isEnabled("productionCounting")) {
            params.setField("registerQuantityInProduct", true);
            params.setField("registerQuantityOutProduct", true);
            params.setField("registerProductionTime", true);
            params.setField("justOne", false);
            params.setField("allowToClose", false);
            params.setField("autoCloseOrder", false);
        }

        if (isEnabled("qualityControls")) {
            params.setField("checkDoneOrderForQuality", false);
            params.setField("autoGenerateQualityControl", false);
        }

        if (isEnabled("genealogies")) {
            params.setField("batchForDoneOrder", "01none");
        }

        if (isEnabled("advancedGenealogy")) {
            params.setField("batchNumberUniqueness", "01globally");
        }

        if (isEnabled("advancedGenealogyForOrders")) {
            params.setField("trackingRecordForOrderTreatment", "01duringProduction");
        }

        params = params.getDataDefinition().save(params);

        validateEntity(params);
    }

    void addCompany(final Map<String, String> values) {
        Entity company = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_COMPANY).create();

        LOG.debug("id: " + values.get("id") + "number: " + values.get("number") + " companyFullName "
                + values.get("companyfullname") + " tax " + values.get("tax") + " street " + values.get("street") + " house "
                + values.get("house") + " flat " + values.get("flat") + " zipCode " + values.get("zipcode") + " city "
                + values.get("city") + " state " + values.get("state") + " country " + values.get("country") + " email "
                + values.get(EMAIL) + " website " + values.get("website") + " phone " + values.get("phone") + " owner "
                + values.get("owner"));

        company.setField("number", values.get("number"));
        company.setField(NAME, values.get(NAME));
        company.setField("tax", values.get("tax"));
        company.setField("street", values.get("street"));
        company.setField("house", values.get("house"));
        company.setField("flat", values.get("flat"));
        company.setField("zipCode", values.get("zipcode"));
        company.setField("city", values.get("city"));
        company.setField("state", values.get("state"));
        company.setField("country", values.get("country"));
        company.setField(EMAIL, values.get(EMAIL));
        company.setField("website", values.get("website"));
        company.setField("phone", values.get("phone"));
        company.setField("owner", values.get("owner"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test company item {company=" + company.getField(NAME) + "}");
        }

        company = company.getDataDefinition().save(company);
        validateEntity(company);
    }

    void addUser(final Map<String, String> values) {
        Entity user = dataDefinitionService.get("qcadooSecurity", "user").create();
        user.setField("userName", values.get("login"));
        user.setField(EMAIL, values.get(EMAIL));
        user.setField("firstName", values.get("firstname"));
        user.setField("lastName", values.get("lastname"));
        user.setField("password", "123");
        user.setField("passwordConfirmation", "123");
        user.setField("enabled", true);

        SecurityRole role = securityRolesService.getRoleByIdentifier(values.get("role"));
        user.setField("role", role.getName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test user {login=" + user.getField("userName") + ", email=" + user.getField(EMAIL) + ", firstName="
                    + user.getField("firstName") + ", lastName=" + user.getField("lastName") + ", role=" + user.getField("role")
                    + "}");
        }

        user = user.getDataDefinition().save(user);
        validateEntity(user);
    }

    void addShifts(final Map<String, String> values) {
        Entity shift = dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_SHIFT)
                .create();

        shift.setField(NAME, values.get(NAME));
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test shift item {shift=" + shift.getField(NAME) + "}");
        }

        shift = shift.getDataDefinition().save(shift);
        validateEntity(shift);
    }

    String getRandomDictionaryItem(final String dictionaryName) {
        Entity dictionary = getDictionaryByName(dictionaryName);
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .add(SearchRestrictions.belongsTo("dictionary", dictionary));
        int total = searchBuilder.list().getTotalNumberOfEntities();
        return searchBuilder.setMaxResults(1).setFirstResult(RANDOM.nextInt(total)).uniqueResult().getField(NAME).toString();
    }

    Entity getDictionaryByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq(NAME, name))
                .setMaxResults(1).uniqueResult();
    }

    boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }

    void validateEntity(final Entity entity) {
        if (!entity.isValid()) {
            Map<String, ErrorMessage> errors = entity.getErrors();
            List<ErrorMessage> globalErrors = entity.getGlobalErrors();
            Set<String> keys = errors.keySet();
            StringBuilder stringError = new StringBuilder("Saved entity is invalid\n");
            stringError.append("Global errors:\n");
            for (ErrorMessage error : globalErrors) {
                stringError.append(error.getMessage()).append("\nError vars:\n");
                String[] vars = error.getVars();
                for (String errorVar : vars) {
                    stringError.append("\t").append(errorVar).append("\n");
                }
            }
            stringError.append("Errors:\n");
            for (String key : keys) {
                stringError.append("\t").append(key).append("  -  ").append(errors.get(key).getMessage()).append("\nError vars:");
                String[] vars = errors.get(key).getVars();
                for (String errorVar : vars) {
                    stringError.append("\t").append(errorVar).append("\n");
                }
            }
            stringError.append("Fields:\n");
            Map<String, Object> fields = entity.getFields();
            for (Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    stringError.append("\t\t");
                }
                stringError.append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
            }
            throw new IllegalStateException("Saved entity is invalid\n" + stringError.toString());
        }
    }

    private InputStream getXmlFile(final String dataset, final String object, final String locale) throws IOException {
        return TestSamplesLoader.class.getResourceAsStream("/com/qcadoo/mes/samples/" + dataset + "/" + object + "_" + locale
                + ".xml");
    }

}
