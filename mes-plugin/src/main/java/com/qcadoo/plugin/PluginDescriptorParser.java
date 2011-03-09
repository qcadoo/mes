package com.qcadoo.plugin;

import java.io.File;
import java.util.Set;

public interface PluginDescriptorParser {

    Plugin parse(final File file);

    Set<Plugin> loadPlugins();

}
