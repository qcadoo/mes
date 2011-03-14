package com.qcadoo.plugin.internal.api;

import java.util.Collection;
import java.util.List;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

public interface PluginDependencyManager {

    PluginDependencyResult getDependenciesToEnable(List<Plugin> plugins);

    PluginDependencyResult getDependenciesToDisable(List<Plugin> plugins);

    List<Plugin> sortPluginsInDependencyOrder(Collection<Plugin> plugins);

    PluginDependencyResult getDependenciesToUninstall(List<Plugin> plugins);

    PluginDependencyResult getDependenciesToUpdate(Plugin existingPlugin, Plugin newPlugin);

}
