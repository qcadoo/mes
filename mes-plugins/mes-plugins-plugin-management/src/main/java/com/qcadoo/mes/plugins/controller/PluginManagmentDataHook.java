package com.qcadoo.mes.plugins.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PluginManagmentDataHook {

    @Autowired
    private PluginManagmentConnector pluginManagmentConnector;

    public void updatePluginData(final DataDefinition dataDefinition, final Entity entity) {

        String pluginIdentifier = entity.getStringField("identifier");

        PluginAdditionalData pluginAdditionalData = pluginManagmentConnector.getPluginData(pluginIdentifier);

        entity.setField("name", pluginAdditionalData.getName());
        entity.setField("description", pluginAdditionalData.getDescription());
        entity.setField("vendor", pluginAdditionalData.getVendor());
        entity.setField("vendorUrl", pluginAdditionalData.getVendorUrl());
        if (pluginAdditionalData.isSystem()) {
            entity.setField("isSystem", "1");
        } else {
            entity.setField("isSystem", "0");
        }

        pluginManagmentConnector.getPluginData(pluginIdentifier);
    }
}
