package com.qcadoo.plugin.manager;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginAccessor;
import com.qcadoo.plugin.PluginArtifact;
import com.qcadoo.plugin.PluginDao;
import com.qcadoo.plugin.PluginDependencyManager;
import com.qcadoo.plugin.PluginDependencyResult;
import com.qcadoo.plugin.PluginDescriptorParser;
import com.qcadoo.plugin.PluginException;
import com.qcadoo.plugin.PluginFileManager;
import com.qcadoo.plugin.PluginManager;
import com.qcadoo.plugin.PluginOperationResult;
import com.qcadoo.plugin.PluginServerManager;
import com.qcadoo.plugin.PluginState;

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

        pluginDao.save(plugin);
        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin));

        if (!pluginDependencyResult.isDependenciesSatisfied() && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
            return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
        } else {
            return PluginOperationResult.success();
        }
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
    public PluginOperationResult updatePlugin(final PluginArtifact pluginArtifact) {
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

        // TODO KRNA check system plugin
        boolean shouldRestart = false;

        PluginDependencyResult pluginDependencyResult = pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin));

        // TODO KRNA get appropriate
        Plugin databasePlugin = pluginAccessor.getPlugin(plugin.getIdentifier());
        if (databasePlugin.hasState(PluginState.TEMPORARY)) {
            if (!pluginDependencyResult.isDependenciesSatisfied()
                    && !pluginDependencyResult.getUnsatisfiedDependencies().isEmpty()) {
                pluginFileManager.uninstallPlugin(databasePlugin.getFilename());
                // TODO pluginDao.save(plugin) == pluginDao.save(databasePlugin)
                // don't use setPluginInformation
                databasePlugin.setPluginInformation(plugin.getPluginInformation());
                pluginDao.save(databasePlugin);
                return PluginOperationResult.successWithMissingDependencies(pluginDependencyResult);
            }
        } else if (databasePlugin.hasState(PluginState.DISABLED)) {
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
            // TODO KRNA cyclic dependency
        } else if (databasePlugin.hasState(PluginState.ENABLED)) {
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
        pluginFileManager.uninstallPlugin(databasePlugin.getFilename());
        // TODO pluginDao.save(plugin) == pluginDao.save(databasePlugin)
        // don't use setPluginInformation
        databasePlugin.setPluginInformation(plugin.getPluginInformation());
        pluginDao.save(databasePlugin);
        if (shouldRestart) {
            pluginServerManager.restart();
            return PluginOperationResult.successWithRestart();
        } else {
            return PluginOperationResult.success();
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
