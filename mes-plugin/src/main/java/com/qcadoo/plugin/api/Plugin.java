package com.qcadoo.plugin.api;

import java.util.Set;

public interface Plugin {

    String getIdentifier();

    Version getVersion();

    PluginState getState();

    PluginInformation getPluginInformation();

    Set<PluginDependencyInformation> getRequiredPlugins();

    boolean isSystemPlugin();

    void changeStateTo(PluginState state);

    String getFilename();

    int compareVersion(Version version);

    boolean hasState(PluginState expectedState);

    void init();

}
