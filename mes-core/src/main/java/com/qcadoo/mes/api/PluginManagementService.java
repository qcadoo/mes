package com.qcadoo.mes.api;

import org.springframework.web.multipart.MultipartFile;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

public interface PluginManagementService {

    PluginsPlugin getByIdentifier(final String identifier);

    PluginsPlugin getByIdentifierAndStatus(String identifier, String status);

    PluginsPlugin getByEntityId(String entityId);

    PluginsPlugin getByNameAndVendor(final String name, final String vendor);

    void save(PluginsPlugin plugin);

    String downloadPlugin(final MultipartFile file);

    String removePlugin(final String entityId);

    String enablePlugin(final String entityId);

    String restartServer();

    String disablePlugin(final String entityId);

    String deinstallPlugin(final String entityId);

    String updatePlugin(final MultipartFile file);

}
