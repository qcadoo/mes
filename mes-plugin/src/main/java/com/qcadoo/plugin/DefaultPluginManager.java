package com.qcadoo.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultPluginManager implements PluginManager {

    private PluginAccessor pluginAccessor;

    private PluginDao pluginDao;

    private PluginFileManager pluginFileManager;

    private PluginServerManager pluginServerManager;

    private PluginDependencyManager pluginDependencyManager;

    private PluginDescriptorParser pluginDescriptorParser;

    @Override
    public PluginOperationResult enablePlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (!plugin.hasState(PluginState.ENABLED)) {
                plugins.add(plugin);
            }
        }

        if (plugins.isEmpty()) {
            return PluginOperationResult.success();
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied()) {
            if (!pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
            }

            if (!pluginDependencyResult.getDisabledDependencies().isEmpty()) {
                return PluginOperationResult.disabledDependencies(pluginDependencyResult);
            }
        }

        boolean shouldRestart = false;

        List<String> fileNames = new ArrayList<String>();
        for (Plugin plugin : plugins) {
            if (plugin.hasState(PluginState.TEMPORARY)) {
                fileNames.add(plugin.getFilename());
            }
        }
        if (!fileNames.isEmpty()) {
            if (!pluginFileManager.installPlugin(fileNames.toArray(new String[fileNames.size()]))) {
                return PluginOperationResult.cannotInstallPlugin();
            }
            shouldRestart = true;
        }

        for (Plugin plugin : plugins) {
            if (plugin.hasState(PluginState.TEMPORARY)) {
                plugin.changeStateTo(PluginState.ENABLING);
            } else {
                plugin.changeStateTo(PluginState.ENABLED);
            }
            pluginDao.save(plugin);
        }

        if (shouldRestart) {
            pluginServerManager.restart();
            return PluginOperationResult.successWithRestart();
        } else {
            return PluginOperationResult.success();
        }

    }

    @Override
    public PluginOperationResult disablePlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (plugin.isSystemPlugin()) {
                return PluginOperationResult.systemPluginDisabling();
            }

            if (plugin.hasState(PluginState.ENABLED)) {
                plugins.add(plugin);
            }
        }

        if (plugins.isEmpty()) {
            return PluginOperationResult.success();
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToDisable(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied() && !pluginDependencyResult.getEnabledDependencies().isEmpty()) {
            return PluginOperationResult.enabledDependencies(pluginDependencyResult);
        }

        for (Plugin plugin : plugins) {
            plugin.changeStateTo(PluginState.DISABLED);
            pluginDao.save(plugin);
        }

        return PluginOperationResult.success();
    }

    @Override
    public PluginOperationResult uninstallPlugin(final String... keys) {
        List<Plugin> plugins = new ArrayList<Plugin>();

        for (String key : keys) {
            Plugin plugin = pluginAccessor.getPlugin(key);

            if (plugin.isSystemPlugin()) {
                return PluginOperationResult.systemPluginUninstalling();
            }

            plugins.add(plugin);
        }

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToDisable(plugins);

        if (!pluginDependencyResult.isDependenciesSatisfied() && !pluginDependencyResult.getEnabledDependencies().isEmpty()) {
            return PluginOperationResult.enabledDependencies(pluginDependencyResult);
        }

        boolean shouldRestart = false;

        List<String> fileNames = new ArrayList<String>();
        for (Plugin plugin : plugins) {
            if (!plugin.hasState(PluginState.TEMPORARY)) {
                shouldRestart = true;
            }
            fileNames.add(plugin.getFilename());
        }

        if (!pluginFileManager.uninstallPlugin(fileNames.toArray(new String[fileNames.size()]))) {
            return PluginOperationResult.cannotUninstallPlugin();
        }

        for (Plugin plugin : plugins) {
            pluginDao.delete(plugin);
        }

        if (shouldRestart) {
            pluginServerManager.restart();
            return PluginOperationResult.successWithRestart();
        } else {
            return PluginOperationResult.success();
        }
    }

    @Override
    public PluginOperationResult installPlugin(final PluginArtifact pluginArtifact) {
        File pluginFile = null;
        try {
            pluginFile = pluginFileManager.uploadPlugin(pluginArtifact);
        } catch (PluginException e) {
            return PluginOperationResult.cannotUploadPlugin();
        }
        Plugin plugin = null;
        try {
            plugin = pluginDescriptorParser.parse(pluginFile);
        } catch (PluginException e) {
            pluginFileManager.removePlugin(pluginFile.getName());
            return PluginOperationResult.corruptedPlugin();
        }

        if (plugin.isSystemPlugin()) {
            pluginFileManager.removePlugin(plugin.getFilename());
            return PluginOperationResult.systemPluginUpdating();
        }

        boolean shouldRestart = false;

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin));

        Plugin existingPlugin = pluginAccessor.getPlugin(plugin.getIdentifier());
        if (existingPlugin == null) {
            plugin.changeStateTo(PluginState.TEMPORARY);
            pluginDao.save(plugin);

            if (!pluginDependencyResult.isDependenciesSatisfied()
                    && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
            } else {
                return PluginOperationResult.success();
            }
        } else {
            if (existingPlugin.hasState(PluginState.TEMPORARY)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()
                        && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                    pluginFileManager.uninstallPlugin(existingPlugin.getFilename());
                    plugin.changeStateTo(existingPlugin.getPluginState());
                    pluginDao.save(plugin);
                    return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
                }
            } else if (existingPlugin.hasState(PluginState.DISABLED)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()
                        && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                    pluginFileManager.removePlugin(plugin.getFilename());
                    return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
                }
                if (!pluginFileManager.installPlugin(new String[] { plugin.getFilename() })) {
                    pluginFileManager.removePlugin(plugin.getFilename());
                    return PluginOperationResult.cannotInstallPlugin();
                }
                shouldRestart = true;
            } else if (existingPlugin.hasState(PluginState.ENABLED)) {
                if (!pluginDependencyResult.isDependenciesSatisfied()) {
                    if (!pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                        pluginFileManager.removePlugin(plugin.getFilename());
                        return PluginOperationResult.unsatisfiedDependencies(pluginDependencyResult);
                    }

                    if (!pluginDependencyResult.getDisabledDependencies().isEmpty()) {
                        pluginFileManager.removePlugin(plugin.getFilename());
                        return PluginOperationResult.disabledDependencies(pluginDependencyResult);
                    }
                }
                // TODO KRNA disable/enable
            }
            pluginFileManager.uninstallPlugin(existingPlugin.getFilename());
            plugin.changeStateTo(existingPlugin.getPluginState());
            pluginDao.save(plugin);
            if (shouldRestart) {
                pluginServerManager.restart();
                return PluginOperationResult.successWithRestart();
            } else {
                return PluginOperationResult.success();
            }
        }
    }

    public void setPluginAccessor(final PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;

    }

    public void setPluginDao(final PluginDao pluginDao) {
        this.pluginDao = pluginDao;

    }

    public void setPluginFileManager(final PluginFileManager pluginFileManager) {
        this.pluginFileManager = pluginFileManager;
    }

    public void setPluginServerManager(final PluginServerManager pluginServerManager) {
        this.pluginServerManager = pluginServerManager;
    }

    public void setPluginDependencyManager(final PluginDependencyManager pluginDependencyManager) {
        this.pluginDependencyManager = pluginDependencyManager;

    }

    public void setPluginDescriptorParser(final PluginDescriptorParser pluginDescriptorParser) {
        this.pluginDescriptorParser = pluginDescriptorParser;
    }

}
