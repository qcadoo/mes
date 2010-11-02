package com.qcadoo.mes.api;

import org.springframework.web.multipart.MultipartFile;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

public interface PluginManagementService {

    PluginsPlugin getByIdentifier(final String identifier);

    PluginsPlugin getByIdentifierAndStatus(String identifier, String status);

    PluginsPlugin getByEntityId(String entityId);

    PluginsPlugin getByNameAndVendor(final String name, final String vendor);

    void save(PluginsPlugin plugin);

    PluginManagementOperationStatus downloadPlugin(final MultipartFile file);

    PluginManagementOperationStatus removePlugin(final String entityId);

    PluginManagementOperationStatus enablePlugin(final String entityId);

    String restartServer();

    PluginManagementOperationStatus disablePlugin(final String entityId);

    PluginManagementOperationStatus deinstallPlugin(final String entityId);

    PluginManagementOperationStatus updatePlugin(final MultipartFile file);

}
