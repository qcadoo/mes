package com.qcadoo.plugin;

import java.util.Set;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;

public interface Plugin extends PersistentPlugin {

    PluginInformation getPluginInformation();

    Set<PluginDependencyInformation> getRequiredPlugins();

    boolean isSystemPlugin();

    void changeStateTo(PluginState state);

    String getFilename();

    int compareVersion(PersistentPlugin plugin);

    void init();

}
