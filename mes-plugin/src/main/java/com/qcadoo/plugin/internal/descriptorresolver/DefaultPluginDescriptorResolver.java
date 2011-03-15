package com.qcadoo.plugin.internal.descriptorresolver;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorResolver implements PluginDescriptorResolver {

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Value("#{plugin.descriptors}")
    private String descriptor;

    @Override
    public Resource[] getDescriptors() {
        try {
            return resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find classpath resources for "
                    + ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor, e);
        }
    }

    @Override
    public Resource getDescriptor(final File file) {
        try {
            JarFile jar = new JarFile(file);

            JarEntry entry = jar.getJarEntry("plugin.xml");

            if (entry == null) {
                throw new IllegalStateException("Plugin descriptor plugin.xml not found in " + file.getAbsolutePath());
            }

            return new InputStreamResource(jar.getInputStream(entry));
        } catch (IOException e) {
            throw new IllegalStateException("Plugin descriptor plugin.xml not found in " + file.getAbsolutePath(), e);
        }
    }

    public void setDescriptor(final String descriptor) {
        this.descriptor = descriptor;
    }

}
