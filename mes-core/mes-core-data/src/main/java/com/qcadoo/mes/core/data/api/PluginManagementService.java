package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

public interface PluginManagementService {

    List<PluginsPlugin> getPluginsWithStatus(String status);

    PluginsPlugin getPluginWithStatus(String identifier, String status);

    PluginsPlugin getPlugin(String entityId);

    void savePlugin(PluginsPlugin plugin);

    PluginsPlugin getInstalledPlugin(PluginsPlugin plugin);

}
