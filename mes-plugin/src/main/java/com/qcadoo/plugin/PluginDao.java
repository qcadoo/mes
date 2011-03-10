package com.qcadoo.plugin;

import java.util.Map;
import java.util.Set;

public interface PluginDao {

    void save(String identifier, PluginState state);

    @Deprecated
    void save(Plugin plugin);

    void delete(String identifier);

    @Deprecated
    void delete(Plugin plugin);

    Map<String, PluginState> all();

    @Deprecated
    Set<Plugin> list();

}
