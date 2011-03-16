package com.qcadoo.model.internal.module;

import java.io.InputStream;

import org.w3c.dom.Document;

public interface ModelXmlHolder {

    void put(String plugin, String name, InputStream stream);

    Document get(String plugin, String name);

}
