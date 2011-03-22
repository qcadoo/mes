package com.qcadoo.plugin.internal.api;

import com.qcadoo.plugin.api.PluginState;

public abstract class Module {

    public abstract void init(PluginState state);

    public abstract void enable();

    public abstract void disable();

}
