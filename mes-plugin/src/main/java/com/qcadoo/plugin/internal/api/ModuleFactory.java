package com.qcadoo.plugin.internal.api;

import org.w3c.dom.Node;

public interface ModuleFactory<T extends Module> {

    void postInitialize();

    T parse(final Node node);

    String getIdentifier();

}