package com.qcadoo.mes.plugins.controller;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PluginManagmentDataHook {

    public void updatePluginData(final DataDefinition dataDefinition, final Entity entity) {

        String pluginIdentifier = entity.getStringField("identifier");

        entity.setField("name", pluginIdentifier + " name");
        entity.setField("isSystem", "" + RandomUtils.nextInt(2));
        entity.setField("description", pluginIdentifier + " description");
        entity.setField("vendor", pluginIdentifier + " vendor");
        entity.setField("vendorUrl", pluginIdentifier + " vendor url");
    }
}
