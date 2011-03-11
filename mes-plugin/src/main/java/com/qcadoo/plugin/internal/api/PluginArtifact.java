package com.qcadoo.plugin.internal.api;

import java.io.InputStream;

public interface PluginArtifact {

    String getName();

    InputStream getInputStream();

}
