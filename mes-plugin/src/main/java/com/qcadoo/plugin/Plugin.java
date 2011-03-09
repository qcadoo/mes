package com.qcadoo.plugin;

import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public interface Plugin {

    String getIdentifier();

    PluginInformation getPluginInformation();

    PluginState getPluginState();

    Set<PluginDependencyInformation> getRequiredPlugins();

    boolean isSystemPlugin();

    boolean hasState(PluginState state);

    Object changeStateTo(PluginState disabled);

    String getFilename();

    int compareVersion(Plugin plugin);

    void init();

}
