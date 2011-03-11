package com.qcadoo.plugin.internal.api;

import java.util.Set;

import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.Plugin;

public interface PluginDescriptorParser {

    Plugin parse(final Resource resource);

    Set<Plugin> loadPlugins();

}
