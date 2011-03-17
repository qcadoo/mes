package com.qcadoo.plugin.internal.api;

import com.qcadoo.plugin.api.PluginState;

public interface Module {

    void init(PluginState state);

    void enable();

    void disable();

}
