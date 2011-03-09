package com.qcadoo.plugin;

import java.util.Set;

public interface PluginDao {

    void save(Plugin plugin);

    void delete(Plugin plugin);

    Set<Plugin> list();

}
