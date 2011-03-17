package com.qcadoo.mes.plugins.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.plugin.api.PluginServerManager;
import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginOperationResult;

@Service
public class PluginManagmentConnector {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private PluginServerManager pluginServerManager;

    public PluginOperationResult performInstall(final PluginArtifact artifact) {
        return pluginManager.installPlugin(artifact);
    }

    public PluginOperationResult performEnable(final List<String> pluginIdentifiers) {
        return pluginManager.enablePlugin(pluginIdentifiers.toArray(new String[pluginIdentifiers.size()]));
    }

    public PluginOperationResult performDisable(final List<String> pluginIdentifiers) {
        return pluginManager.disablePlugin(pluginIdentifiers.toArray(new String[pluginIdentifiers.size()]));
    }

    public PluginOperationResult performRemove(final List<String> pluginIdentifiers) {
        return pluginManager.uninstallPlugin(pluginIdentifiers.toArray(new String[pluginIdentifiers.size()]));
    }

    public PluginAdditionalData getPluginData(final String pluginIdentifier) {
        Plugin plugin = pluginAccessor.getPlugin(pluginIdentifier);
        return new PluginAdditionalData(plugin.getPluginInformation().getName(), plugin.getPluginInformation().getDescription(),
                plugin.getPluginInformation().getVendor(), plugin.getPluginInformation().getVendorUrl(), plugin.isSystemPlugin());
    }

    public void performRestart() {
        pluginServerManager.restart();
    }

}
