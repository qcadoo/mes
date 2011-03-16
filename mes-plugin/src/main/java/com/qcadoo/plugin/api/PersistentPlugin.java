package com.qcadoo.plugin.api;


public interface PersistentPlugin {

    String getIdentifier();

    PluginState getState();

    Version getVersion();

    boolean hasState(PluginState expectedState);

}