package com.qcadoo.plugin.internal.api;

import java.util.Set;

import com.qcadoo.model.beans.plugins.PluginsPlugin;
import com.qcadoo.plugin.api.Plugin;

public interface PluginDao {

    void save(PluginsPlugin plugin);

    void save(Plugin plugin);

    void delete(PluginsPlugin plugin);

    void delete(Plugin plugin);

    Set<PluginsPlugin> list();

}
