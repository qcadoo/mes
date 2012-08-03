/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static com.qcadoo.mes.samples.constants.ParameterFieldsSamples.SAMPLES_WERE_LOADED;

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

    protected static final String NAME = "name";

    protected static final String ITEM = "item";

    protected static final String EMAIL = "email";

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
        Entity dictionary = getDictionaryByName(values.get(NAME));

        Entity item = dataDefinitionService.get("qcadooModel", "dictionaryItem").create();
        item.setField("dictionary", dictionary);
        item.setField(NAME, values.get("item"));
        item.setField("description", values.get("description"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add test dictionary item {dictionary=" + dictionary.getField(NAME) + ", item=" + item.getField(NAME)
                    + ", description=" + item.getField("description") + "}");
        }

        item.getDataDefinition().save(item);
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

    protected void addParameters(final Map<String, String> values) {
        LOG.info("Adding parameters");
        Entity params = parameterService.getParameter();

        Entity currency = dataDefinitionService
                .get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_CURRENCY).find()
                .add(SearchRestrictions.eq("alphabeticCode", values.get("code"))).uniqueResult();

        params.setField("currency", currency);
        params.setField("unit", values.get("unit"));

        if (isEnabledOrEnabling("productionCounting")) {
            params.setField("registerQuantityInProduct", true);
            params.setField("registerQuantityOutProduct", true);
            params.setField("registerProductionTime", true);
            params.setField("justOne", false);
            params.setField("allowToClose", false);
            params.setField("autoCloseOrder", false);
        }

        if (isEnabledOrEnabling("qualityControls")) {
            params.setField("checkDoneOrderForQuality", false);
            params.setField("autoGenerateQualityControl", false);
        }

        if (isEnabledOrEnabling("genealogies")) {
            params.setField("batchForDoneOrder", "01none");
        }

        if (isEnabledOrEnabling("advancedGenealogy")) {
            params.setField("batchNumberUniqueness", "01globally");
        }

        if (isEnabledOrEnabling("advancedGenealogyForOrders")) {
            params.setField("trackingRecordForOrderTreatment", "01duringProduction");
            params.setField("batchNumberRequiredInputProducts", false);
        }

        params.getDataDefinition().save(params);
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

        company.getDataDefinition().save(company);
    }

}
