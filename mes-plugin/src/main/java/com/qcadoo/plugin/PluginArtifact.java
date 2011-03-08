package com.qcadoo.plugin;

import java.io.InputStream;

public interface PluginArtifact {

    String getName();

    InputStream getInputStream();

}
