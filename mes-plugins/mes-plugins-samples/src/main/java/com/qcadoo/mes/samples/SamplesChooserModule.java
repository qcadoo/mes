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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.samples.constants.SamplesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.api.Module;

@Component
public class SamplesChooserModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(SamplesChooserModule.class);

    @Autowired
    private GeneratedSamplesLoader generatedSamplesLoaderModule;

    @Autowired
    private TestSamplesLoader testSamplesLoaderModule;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MinimalSamplesLoader minimalSamplesLoaderModule;

    @Value("${samplesDataset}")
    private String samplesDataset;

    @Value("${samplesDatasetLocale}")
    private String locale;

    @Override
    @Transactional
    public void multiTenantEnable() {
        if (databaseHasToBePrepared() && !"NONE".equals(samplesDataset.toUpperCase())) {
            LOG.debug("Database has to be prepared ...");
            setLocale();
            if ("TEST".equals(samplesDataset.toUpperCase())) {
                testSamplesLoaderModule.loadData("test", locale);
            } else if ("GENERATED".equals(samplesDataset.toUpperCase())) {
                generatedSamplesLoaderModule.loadData("generated", locale);
            } else if ("MINIMAL".equals(samplesDataset.toUpperCase())) {
                minimalSamplesLoaderModule.loadData("minimal", locale);
            }
        } else {
            LOG.debug("Database won't bo changed ... ");
        }
    }

    private void setLocale() {
        if ("default".equals(locale)) {
            locale = Locale.getDefault().toString().substring(0, 2);
        }

        if (!("pl".equals(locale) || "en".equals(locale))) {
            locale = "en";
        }
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get(SamplesConstants.BASIC_PLUGIN_IDENTIFIER, SamplesConstants.BASIC_MODEL_PARAMETER).find()
                .list().getTotalNumberOfEntities() == 0;
    }
}
