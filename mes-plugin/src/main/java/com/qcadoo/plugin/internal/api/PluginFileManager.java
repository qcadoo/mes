package com.qcadoo.plugin.internal.api;

import org.springframework.core.io.Resource;

public interface PluginFileManager {

    public boolean installPlugin(final String... filename);

    public Resource uploadPlugin(final PluginArtifact pluginArtifact);

    public void uninstallPlugin(final String... filename);

}
