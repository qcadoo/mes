package com.qcadoo.plugin.api;

import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginOperationResult;

public interface PluginManager {

    PluginOperationResult enablePlugin(final String... keys);

    PluginOperationResult disablePlugin(final String... keys);

    PluginOperationResult uninstallPlugin(final String... keys);

    PluginOperationResult installPlugin(final PluginArtifact pluginArtifact);
}
