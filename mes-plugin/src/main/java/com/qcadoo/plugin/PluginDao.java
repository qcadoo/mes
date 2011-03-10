package com.qcadoo.plugin;

import java.util.Set;

public interface PluginDao {

    void save(PersistentPlugin plugin);

    void delete(PersistentPlugin plugin);

    Set<PersistentPlugin> list();

}
