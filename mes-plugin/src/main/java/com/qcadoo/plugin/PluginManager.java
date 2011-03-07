package com.qcadoo.plugin;

public interface PluginManager {

    PluginOperationResult enablePlugin(final String... keys);

    PluginOperationResult disablePlugin(final String... keys);

    PluginOperationResult installPlugin(final PluginArtifact pluginArtifact);

    PluginOperationResult uninstallPlugin(final String... keys);

    PluginOperationResult updatePlugin(final PluginArtifact pluginArtifact);
}
