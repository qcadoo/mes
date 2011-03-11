package com.qcadoo.plugin.internal.servermanager;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.PluginServerManager;

public class DefaultPluginServerManager implements PluginServerManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginServerManager.class);

    private String restartCommand;

    @Override
    public void restart() {
        try {
            Process shutdownProcess = Runtime.getRuntime().exec(restartCommand);
            shutdownProcess.waitFor();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Shutdown exit value: " + shutdownProcess.exitValue());
            }
        } catch (IOException e) {
            throw new PluginException("Restart failed - " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new PluginException("Restart failed - " + e.getMessage(), e);
        }
    }

    public void setRestartCommand(final String restartCommand) {
        this.restartCommand = restartCommand;
    }

}
