package com.qcadoo.mes.core.data.api;

import java.util.List;

import com.qcadoo.mes.core.data.beans.Plugin;

public interface PluginManagementService {

    List<Plugin> getPluginsWithStatus(String status);

    Plugin getPluginWithStatus(String identifier, String status);

    Plugin getPlugin(String entityId);

    void savePlugin(Plugin plugin);

    Plugin getInstalledPlugin(Plugin plugin);

}
