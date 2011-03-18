package com.qcadoo.plugin.internal.artifact;

import java.io.InputStream;

import com.qcadoo.plugin.internal.api.PluginArtifact;

public class InputStreamPluginArtifact implements PluginArtifact {

    private final String name;

    private final InputStream inputStream;

    public InputStreamPluginArtifact(final String name, final InputStream inputStream) {
        this.name = name;
        this.inputStream = inputStream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

}
