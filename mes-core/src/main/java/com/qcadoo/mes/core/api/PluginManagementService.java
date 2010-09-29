package com.qcadoo.mes.core.api;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

public interface PluginManagementService {

    List<PluginsPlugin> getActivePlugins();

    PluginsPlugin getPluginByIdentifier(final String identifier);

    PluginsPlugin getPluginByIdentifierAndStatus(String identifier, String status);

    PluginsPlugin getPluginById(String entityId);

    PluginsPlugin getPluginByNameAndVendor(final String name, final String vendor);

    void savePlugin(PluginsPlugin plugin);

    String downloadPlugin(final MultipartFile file);

    String removePlugin(final String entityId);

    String enablePlugin(final String entityId);

    String restartServer();

    String disablePlugin(final String entityId);

    String deinstallPlugin(final String entityId);

    String updatePlugin(final MultipartFile file);

}
