package com.qcadoo.plugin;

public class DefaultPluginServerManager implements PluginServerManager {

    private String restartCommand;

    @Override
    public void restart() {
        // TODO Auto-generated method stub

    }

    public void setRestartCommand(final String restartCommand) {
        this.restartCommand = restartCommand;
    }

}
