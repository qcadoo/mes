package com.qcadoo.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultPluginAccessor implements PluginAccessor {

    private PluginDescriptorParser pluginDescriptorParser;

    private PluginDao pluginDao;

    private final Map<String, Plugin> enabledPlugins = new HashMap<String, Plugin>();

    private final Map<String, Plugin> plugins = new HashMap<String, Plugin>();

    private PluginDependencyManager pluginDependencyManager;

    @Override
    public PersistentPlugin getEnabledPlugin(final String identifier) {
        return enabledPlugins.get(identifier);
    }

    @Override
    public Collection<Plugin> getEnabledPlugins() {
        return enabledPlugins.values();
    }

    @Override
    public Plugin getPlugin(final String identifier) {
        return plugins.get(identifier);
    }

    @Override
    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    public void init() {
        Set<Plugin> pluginsFromDescriptor = pluginDescriptorParser.loadPlugins();
        Set<PersistentPlugin> pluginsFromDatabase = pluginDao.list();

        for (Plugin plugin : pluginsFromDescriptor) {
            PersistentPlugin existingPlugin = null;
            for (PersistentPlugin databasePlugin : pluginsFromDatabase) {
                if (plugin.getIdentifier().equals(databasePlugin.getIdentifier())) {
                    existingPlugin = databasePlugin;
                    break;
                }
            }
            if (existingPlugin != null) {
                plugin.changeStateTo(existingPlugin.getPluginState());
            } else {
                plugin.changeStateTo(PluginState.DISABLED);
            }
            if (existingPlugin == null || plugin.compareVersion(existingPlugin) > 0) {
                pluginDao.save(plugin);
            } else if (plugin.compareVersion(existingPlugin) < 0) {
                throw new IllegalStateException("Unsupported operation: downgrade, for plugin: " + plugin.getIdentifier());
            }
            plugins.put(plugin.getIdentifier(), plugin);
            if (plugin.hasState(PluginState.ENABLED)) {
                enabledPlugins.put(plugin.getIdentifier(), plugin);
            }
        }
        for (PersistentPlugin databasePlugin : pluginsFromDatabase) {
            if (databasePlugin.hasState(PluginState.TEMPORARY)) {
                continue;
            }

            PersistentPlugin existingPlugin = null;
            for (PersistentPlugin plugin : pluginsFromDescriptor) {
                if (databasePlugin.getIdentifier().equals(plugin.getIdentifier())) {
                    existingPlugin = plugin;
                    break;
                }
            }
            if (existingPlugin == null) {
                pluginDao.delete(databasePlugin);
            }
        }

        for (Plugin plugin : pluginDependencyManager.sortPluginsInDependencyOrder(plugins.values())) {
            if (plugin.hasState(PluginState.ENABLING)) {
                plugin.changeStateTo(PluginState.ENABLED);
                pluginDao.save(plugin);
            }
            plugin.init();
        }

    }

    public void setPluginDescriptorParser(final PluginDescriptorParser pluginDescriptorParser) {
        this.pluginDescriptorParser = pluginDescriptorParser;
    }

    public void setPluginDao(final PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    public void setPluginDependencyManager(final PluginDependencyManager pluginDependencyManager) {
        this.pluginDependencyManager = pluginDependencyManager;
    }
}
