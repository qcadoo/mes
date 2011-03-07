package com.qcadoo.plugin;

import java.util.List;

public interface PluginDependencyManager {

    PluginDependencyResult getDependenciesToEnable(List<Plugin> plugins);

    PluginDependencyResult getDependenciesToDisable(List<Plugin> plugins);

}
