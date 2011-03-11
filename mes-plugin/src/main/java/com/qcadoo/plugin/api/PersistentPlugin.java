package com.qcadoo.plugin.api;


public interface PersistentPlugin {

    String getIdentifier();

    PluginState getPluginState();

    Version getVersion();

    boolean hasState(PluginState expectedState);

}