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
package com.qcadoo.mes.samples;

import static com.qcadoo.mes.samples.constants.SamplesConstants.L_DEFAULT_PRODUCTION_LINE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NUMBER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_PRODUCTION_LINES;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_PRODUCTION_LINES_DICTIONARY;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTION_LINES_MODEL_PRODUCTION_LINE;
import static com.qcadoo.mes.samples.constants.SamplesConstants.PRODUCTION_LINES_PLUGIN_IDENTIFIER;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.LocaleUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.security.api.SecurityRole;
import com.qcadoo.security.api.SecurityRolesService;

@Transactional
public abstract class SamplesLoader {

    private static final String NAME = "name";

    private static final String EMAIL = "email";

    protected static final String STATE_ACCEPTED = "02accepted";

    private static final Logger LOG = LoggerFactory.getLogger(SamplesLoader.class);

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityRolesService securityRolesService;

    abstract void loadData(final String dataset, final String locale);

    protected void readData(final Map<String, String> values, final String type, final Element node) {
        if ("activeCurrency".equals(type)) {
            addParameters(values);
        } else if ("dictionaries".equals(type)) {
            addDictionaryItems(values);
        } else if ("shifts".equals(type)) {
            addShifts(values);
        } else if ("company".equals(type)) {
            addCompany(values);
        } else if (L_PRODUCTION_LINES.equals(type)) {
            addProductionLines(values);
        } else if (L_PRODUCTION_LINES_DICTIONARY.equals(type)) {
            addDictionaryItems(values);
        } else if (L_DEFAULT_PRODUCTION_LINE.equals(type)) {
            addDefaultProductionLine(values);
        }
    }

    protected void readDataFromXML(final String dataset, final String type, final String locale) {

        LOG.info("Loading test data from " + type + "_" + locale + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(dataset, type, locale));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {
                Element node = list.get(i);
                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtribute = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();

                for (int j = 0; j < listOfAtribute.size(); j++) {
                    values.put(listOfAtribute.get(j).getName().toLowerCase(LocaleUtils.toLocale(locale)), listOfAtribute.get(j)
                            .getValue());
                }
                readData(values, type, node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void addDictionaryItems(final Map<String, String> values) {
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
    }

    protected void addParameters(final Map<String, String> values) {
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
    }

    protected void addCompany(final Map<String, String> values) {
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
    }

    protected void addUser(final Map<String, String> values) {
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
    }

    protected void addShifts(final Map<String, String> values) {
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
    }

    protected void addProductionLines(final Map<String, String> values) {
        Entity productionLine = dataDefinitionService.get(PRODUCTION_LINES_PLUGIN_IDENTIFIER,
                PRODUCTION_LINES_MODEL_PRODUCTION_LINE).create();
        productionLine.setField(L_NAME, values.get(L_NAME));
        productionLine.setField(L_NUMBER, values.get(L_NUMBER));
        productionLine.setField("supportsAllTechnologies", values.get("supportsalltechnologies"));
        productionLine.setField("supportsOtherTechnologiesWorkstationTypes",
                values.get("supportsothertechnologiesworkstationtypes"));
        productionLine.setField("quantityForOtherWorkstationTypes", values.get("quantityforotherworkstationtypes"));

        productionLine = productionLine.getDataDefinition().save(productionLine);
    }

    protected void addDefaultProductionLine(final Map<String, String> values) {
        Entity parameter = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_PARAMETER).find().uniqueResult();

        parameter.setField(L_DEFAULT_PRODUCTION_LINE, getProductionLineByNumber(values.get("production_line_nr")));

        parameter = parameter.getDataDefinition().save(parameter);
    }

    protected Entity getProductionLineByNumber(final String number) {
        return dataDefinitionService.get(PRODUCTION_LINES_PLUGIN_IDENTIFIER, PRODUCTION_LINES_MODEL_PRODUCTION_LINE).find()
                .add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    protected String getRandomDictionaryItem(final String dictionaryName) {
        Entity dictionary = getDictionaryByName(dictionaryName);
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .add(SearchRestrictions.belongsTo("dictionary", dictionary));
        int total = searchBuilder.list().getTotalNumberOfEntities();
        return searchBuilder.setMaxResults(1).setFirstResult(RANDOM.nextInt(total)).uniqueResult().getField(NAME).toString();
    }

    protected Entity getDictionaryByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq(NAME, name))
                .setMaxResults(1).uniqueResult();
    }

    protected boolean isEnabled(final String pluginIdentifier) {
        return PluginUtils.isEnabled(pluginIdentifier);
    }

    private InputStream getXmlFile(final String dataset, final String object, final String locale) throws IOException {
        return TestSamplesLoader.class.getResourceAsStream("/samples/" + dataset + "/" + object + "_" + locale + ".xml");
    }

}
