package com.qcadoo.plugin;

public class DefaultPersistentPlugin implements PersistentPlugin {

    private final String identifier;

    private final int[] version;

    private PluginState state;

    public DefaultPersistentPlugin(final String identifier, final PluginState state, final int[] version) {
        this.identifier = identifier;
        this.version = version;
        this.state = state;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public PluginState getPluginState() {
        return state;
    }

    protected void setPluginState(final PluginState state) {
        this.state = state;
    }

    @Override
    public boolean hasState(final PluginState expectedState) {
        return state.equals(expectedState);
    }

    @Override
    public int[] getVersion() {
        return version.clone();
    }

}