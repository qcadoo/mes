package com.qcadoo.plugin.internal.api;

import org.jdom.Element;

public interface ModuleFactory<T extends Module> {

    void init();

    T parse(String pluginIdentifier, Element element);

    String getIdentifier();

}