package com.qcadoo.plugin.api;

import java.util.Set;


public interface Plugin extends PersistentPlugin {

    PluginInformation getPluginInformation();

    Set<PluginDependencyInformation> getRequiredPlugins();

    boolean isSystemPlugin();

    void changeStateTo(PluginState state);

    String getFilename();

    int compareVersion(PersistentPlugin plugin);

    void init();

}
