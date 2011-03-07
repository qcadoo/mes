package com.qcadoo.plugin;
import java.io.File;
import java.io.InputStream;

public interface PluginArtifact {

    boolean doesResourceExist(String name);

    InputStream getResourceAsStream(String name);

    String getName();

    InputStream getInputStream();

    File toFile();

}
