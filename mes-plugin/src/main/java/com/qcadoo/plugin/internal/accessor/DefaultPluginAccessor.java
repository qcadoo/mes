package com.qcadoo.plugin.internal.accessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;
import com.qcadoo.plugin.internal.api.PluginDao;
import com.qcadoo.plugin.internal.api.PluginDependencyManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;

@Service
public final class DefaultPluginAccessor implements PluginAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginAccessor.class);

    @Autowired
    private PluginDescriptorParser pluginDescriptorParser;

    @Autowired
    private PluginDao pluginDao;

    @Autowired
    private PluginDependencyManager pluginDependencyManager;

    @Autowired
    private ModuleFactoryAccessor moduleFactoryAccessor;

    private final Map<String, Plugin> enabledPlugins = new HashMap<String, Plugin>();

    private final Map<String, Plugin> plugins = new HashMap<String, Plugin>();

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

    @PostConstruct
    public void init() {
        LOG.info("Plugin Framework initialization");

        long time = System.currentTimeMillis();

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

            LOG.info("Registering plugin " + plugin);

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
            plugin.init();
        }

        moduleFactoryAccessor.postInitialize();

        for (Plugin plugin : pluginDependencyManager.sortPluginsInDependencyOrder(plugins.values())) {
            if (plugin.hasState(PluginState.ENABLING)) {
                plugin.changeStateTo(PluginState.ENABLED);
                pluginDao.save(plugin);
            }
        }

        LOG.info("Plugin Framework initialized in " + (System.currentTimeMillis() - time) + "ms");
    }

    void setPluginDescriptorParser(final PluginDescriptorParser pluginDescriptorParser) {
        this.pluginDescriptorParser = pluginDescriptorParser;
    }

    void setPluginDao(final PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    void setPluginDependencyManager(final PluginDependencyManager pluginDependencyManager) {
        this.pluginDependencyManager = pluginDependencyManager;
    }

    void setModuleFactoryAccessor(final ModuleFactoryAccessor moduleFactoryAccessor) {
        this.moduleFactoryAccessor = moduleFactoryAccessor;
    }
}
