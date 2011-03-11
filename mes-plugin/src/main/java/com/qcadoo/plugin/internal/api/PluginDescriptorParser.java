package com.qcadoo.plugin.internal.api;

import java.io.File;
import java.util.Set;

import com.qcadoo.plugin.api.Plugin;

public interface PluginDescriptorParser {

    Plugin parse(final File file);

    Set<Plugin> loadPlugins();

}
