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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.api.Module;

@Component
public class SamplesChooser extends Module {

    @Autowired
    private GeneratedSamplesLoaderModule generatedSamplesLoaderModule;

    @Autowired
    private TestSamplesLoaderModule testSamplesLoaderModule;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MinimalSamplesLoaderModule minimalSamplesLoaderModule;

    @Value("${samplesDataset}")
    private String samplesDataset;

    private static final Logger LOG = LoggerFactory.getLogger(TestSamplesLoaderModule.class);

    private static final String BASIC_PLUGIN_NAME = "basic";

    private static final String BASIC_MODEL_PARAMETER = "parameter";

    @Override
    public void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            if ("TEST".equals(samplesDataset.toUpperCase())) {
                LOG.debug("Data base has to be prepared ...");
                testSamplesLoaderModule.multiTenantEnable();
            } else if ("GENERATED".equals(samplesDataset.toUpperCase())) {
                LOG.debug("Data base has to be prepared ...");
                generatedSamplesLoaderModule.multiTenantEnable();
            } else if ("MINIMAL".equals(samplesDataset.toUpperCase())) {
                LOG.debug("Data base has to be prepared ...");
                minimalSamplesLoaderModule.multiTenantEnable();
            } else if ("NONE".equals(samplesDataset.toUpperCase())) {
                LOG.debug("Data base won't be changed ...");
            } else {
                throw new IllegalStateException("Invaid loadTestData property!");
            }
        } else {
            LOG.debug("Data base won't bo changed ... ");
        }

    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get(BASIC_PLUGIN_NAME, BASIC_MODEL_PARAMETER).find().list().getTotalNumberOfEntities() == 0;
    }
}
