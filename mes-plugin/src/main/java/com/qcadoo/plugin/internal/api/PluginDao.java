package com.qcadoo.plugin.internal.api;

import java.util.Set;

import com.qcadoo.plugin.api.PersistentPlugin;

public interface PluginDao {

    void save(PersistentPlugin plugin);

    void delete(PersistentPlugin plugin);

    Set<PersistentPlugin> list();

}
