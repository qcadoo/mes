package com.qcadoo.model.internal.module;

import java.io.InputStream;

import org.jdom.Document;

public interface ModelXmlHolder {

    void put(String pluginIdentifier, String modelName, InputStream stream);

    Document get(String pluginIdentifier, String modelName);

}
