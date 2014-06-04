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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * ***************************************************************************
 */
package com.qcadoo.mes.samples.loader;

import static com.qcadoo.mes.samples.constants.ParameterFieldsSamples.SAMPLES_WERE_LOADED;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_COUNTRY;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_EMAIL;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NAME;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_NUMBER;
import static com.qcadoo.mes.samples.constants.SamplesConstants.L_OWNER;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.samples.api.SamplesLoader;
import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.mes.states.service.client.StateChangeSamplesClient;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Transactional
public abstract class AbstractSamplesLoader implements SamplesLoader {

    protected static final Logger LOG = LoggerFactory.getLogger(SamplesLoader.class);

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private StateChangeSamplesClient stateChangeSamplesClient;

    @Override
    @Transactional
    public void load() {
        if (databaseHasToBePrepared()) {
            LOG.debug("Database has to be prepared ...");
            loadData(defaultLocaleResolver.getDefaultLocale().getLanguage());
            markSamplesAsLoaded();
        } else {
            LOG.debug("Database won't be changed ... ");
        }
    }

    /**
     * Load samples data for specified language
     * 
     * @param locale
     *            language code (ISO-639 code). Currently supported language codes are "pl" and "en".
     */
    protected abstract void loadData(final String locale);

    protected StateChangeSamplesClient getStateChangeSamplesClient() {
        return stateChangeSamplesClient;
    }

    private boolean databaseHasToBePrepared() {
        Entity parameter = parameterService.getParameter();
        return !parameter.getBooleanField(SAMPLES_WERE_LOADED);
    }

    private void markSamplesAsLoaded() {
        Entity parameter = parameterService.getParameter();
        parameter.setField(SAMPLES_WERE_LOADED, true);
        parameter.getDataDefinition().save(parameter);
    }

    @SuppressWarnings("deprecation")
    protected boolean isEnabledOrEnabling(final String pluginIdentifier) {
        return PluginUtils.isEnabledOrEnabling(pluginIdentifier);
    }

    protected void addDictionaryItems(final Map<String, String> values) {
        Entity dictionary = getDictionaryByName(values.get(L_NAME));

        if (dictionary != null) {
            Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
            item.setField("dictionary", dictionary);
            item.setField(L_NAME, values.get("item"));
            item.setField("description", values.get("description"));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Add test dictionary item {dictionary=" + dictionary.getField(L_NAME) + ", item="
                        + item.getField(L_NAME) + ", description=" + item.getField("description") + "}");
            }
            item.getDataDefinition().save(item);

        }
    }

    protected String getRandomDictionaryItem(final String dictionaryName) {
        Entity dictionary = getDictionaryByName(dictionaryName);
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .add(SearchRestrictions.belongsTo("dictionary", dictionary));
        int total = searchBuilder.list().getTotalNumberOfEntities();
        return searchBuilder.setMaxResults(1).setFirstResult(RANDOM.nextInt(total)).uniqueResult().getField(L_NAME).toString();
    }

    protected Entity getDictionaryByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionary").find().add(SearchRestrictions.eq(L_NAME, name))
                .setMaxResults(1).uniqueResult();
    }

    protected void addParameters(final Map<String, String> values) {
        LOG.info("Adding parameters");
        Entity parameter = parameterService.getParameter();

        parameter.setField(L_COUNTRY, getCountry(values.get(L_COUNTRY)));
        parameter.setField("currency", getCurrency(values.get("code")));
        parameter.setField("unit", values.get("unit"));
        parameter.setField("company", getCompany(values.get(L_OWNER)));

        if (isEnabledOrEnabling("productionCounting")) {
            parameter.setField("typeOfProductionRecording", "02cumulated");
            parameter.setField("registerQuantityInProduct", true);
            parameter.setField("registerQuantityOutProduct", true);
            parameter.setField("registerProductionTime", true);
            parameter.setField("justOne", false);
            parameter.setField("allowToClose", false);
            parameter.setField("autoCloseOrder", false);
        }

        if (isEnabledOrEnabling("qualityControls")) {
            parameter.setField("checkDoneOrderForQuality", false);
            parameter.setField("autoGenerateQualityControl", false);
        }

        if (isEnabledOrEnabling("genealogies")) {
            parameter.setField("batchForDoneOrder", "01none");
        }

        if (isEnabledOrEnabling("advancedGenealogy")) {
            parameter.setField("batchNumberUniqueness", "01globally");
        }

        if (isEnabledOrEnabling("advancedGenealogyForOrders")) {
            parameter.setField("trackingRecordForOrderTreatment", "01duringProduction");
            parameter.setField("batchNumberRequiredInputProducts", false);
        }

        if (isEnabledOrEnabling("materialRequirements")) {
            parameter.setField("inputProductsRequiredForType", "01startOrder");
        }

        if (isEnabledOrEnabling("materialFlowResources")) {
            parameter.setField("changeDateWhenTransferToWarehouseType", "01never");
        }

        parameter.getDataDefinition().save(parameter);
    }

    protected void addCompany(final Map<String, String> values) {
        Entity company = dataDefinitionService.get(SamplesConstants.L_BASIC_PLUGIN_IDENTIFIER,
                SamplesConstants.L_BASIC_MODEL_COMPANY).create();

        LOG.debug("id: " + values.get("id") + "number: " + values.get(L_NUMBER) + " companyFullName "
                + values.get("companyfullname") + " tax " + values.get("tax") + " street " + values.get("street") + " house "
                + values.get("house") + " flat " + values.get("flat") + " zipCode " + values.get("zipcode") + " city "
                + values.get("city") + " state " + values.get("state") + " country " + values.get(L_COUNTRY) + " email "
                + values.get(L_EMAIL) + " website " + values.get("website") + " phone " + values.get("phone") + " owner "
                + values.get(L_OWNER));

        company.setField(L_NUMBER, values.get(L_NUMBER));
        company.setField(L_NAME, values.get(L_NAME));
        company.setField("tax", values.get("tax"));
        company.setField("street", values.get("street"));
        company.setField("house", values.get("house"));
        company.setField("flat", values.get("flat"));
        company.setField("zipCode", values.get("zipcode"));
        company.setField("city", values.get("city"));
        company.setField("state", values.get("state"));
        company.setField(L_COUNTRY, getCountry(values.get(L_COUNTRY)));
        company.setField(L_EMAIL, values.get(L_EMAIL));
        company.setField("website", values.get("website"));
        company.setField("phone", values.get("phone"));
        company.setField(L_OWNER, values.get(L_OWNER));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test company item {company=" + company.getField(L_NAME) + "}");
        }

        company.getDataDefinition().save(company);
    }

    private Entity getCompany(final String number) {
        return dataDefinitionService.get(SamplesConstants.L_BASIC_PLUGIN_IDENTIFIER, SamplesConstants.L_BASIC_MODEL_COMPANY)
                .find().add(SearchRestrictions.eq(L_NUMBER, number)).setMaxResults(1).uniqueResult();
    }

    private Entity getCurrency(final String code) {
        return dataDefinitionService.get(SamplesConstants.L_BASIC_PLUGIN_IDENTIFIER, SamplesConstants.L_BASIC_MODEL_CURRENCY)
                .find().add(SearchRestrictions.eq("alphabeticCode", code)).setMaxResults(1).uniqueResult();
    }

    private Entity getCountry(final String code) {
        return dataDefinitionService.get(SamplesConstants.L_BASIC_PLUGIN_IDENTIFIER, SamplesConstants.L_BASIC_MODEL_COUNTRY)
                .find().add(SearchRestrictions.eq("code", code)).setMaxResults(1).uniqueResult();
    }

}