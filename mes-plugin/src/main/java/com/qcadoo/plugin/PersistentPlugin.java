package com.qcadoo.plugin;

public interface PersistentPlugin {

    String getIdentifier();

    PluginState getPluginState();

    int[] getVersion();

    boolean hasState(PluginState temporary);

}