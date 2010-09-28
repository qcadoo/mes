package com.qcadoo.mes.core.api;

import java.util.List;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

public interface PluginManagementService {

    List<PluginsPlugin> getActivePlugins();

    PluginsPlugin getPluginByIdentifier(final String identifier);

    PluginsPlugin getPluginByIdentifierAndStatus(String identifier, String status);

    PluginsPlugin getPluginById(String entityId);

    PluginsPlugin getPluginByNameAndVendor(final String name, final String vendor);

    void savePlugin(PluginsPlugin plugin);

}
