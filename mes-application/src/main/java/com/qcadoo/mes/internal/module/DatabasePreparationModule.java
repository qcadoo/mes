package com.qcadoo.mes.internal.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.application.TestDataLoader;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;

public class DatabasePreparationModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePreparationModule.class);

    private DataDefinitionService dataDefinitionService;

    private TestDataLoader testDataLoader;

    private boolean addTestData;

    private void addParameters() {
        LOG.info("Adding parameters");
        Entity parameter = dataDefinitionService.get("basic", "parameter").create();
        parameter.setField("checkDoneOrderForQuality", false);
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("batchForDoneOrder", "01none");
        dataDefinitionService.get("basic", "parameter").save(parameter);
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get("basic", "parameter").find().list().getTotalNumberOfEntities() == 0;
    }

    @Override
    @Transactional
    public void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            LOG.info("Database has to be prepared ...");

            addParameters();

            if (addTestData) {
                testDataLoader.loadTestData();
            }
        } else {
            LOG.info("Database has been already prepared, skipping");
        }
    }

    public void setDataDefinitionService(final DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public void setTestDataLoader(final TestDataLoader testDataLoader) {
        this.testDataLoader = testDataLoader;
    }

    public void setAddTestData(final boolean addTestData) {
        this.addTestData = addTestData;
    }

}
