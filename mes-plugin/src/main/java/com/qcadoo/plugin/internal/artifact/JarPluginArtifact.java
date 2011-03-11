package com.qcadoo.plugin.internal.artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.qcadoo.plugin.internal.api.PluginArtifact;

public final class JarPluginArtifact implements PluginArtifact {

    private final File file;

    public JarPluginArtifact(final File file) {
        if (!file.exists() || !file.canRead()) {
            throw new IllegalStateException("Cannot read file " + file.getAbsolutePath());
        }
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
