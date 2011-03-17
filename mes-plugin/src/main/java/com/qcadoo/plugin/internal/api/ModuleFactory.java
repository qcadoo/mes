package com.qcadoo.plugin.internal.api;

import org.w3c.dom.Node;

public interface ModuleFactory<T extends Module> {

    void postInitialize();

    T parse(String pluginIdentifier, Node node);

    String getIdentifier();

}