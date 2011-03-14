package com.qcadoo.plugin.internal.descriptorresolver;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

public class DefaultPluginDescriptorResolver implements PluginDescriptorResolver {

    private final static String DEFAULT_DESCRIPTOR = "plugin.xml";

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private String descriptor = DEFAULT_DESCRIPTOR;

    @Override
    public Resource[] getDescriptors() {
        try {
            return resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find classpath resources for "
                    + ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor, e);
        }
    }

    public void setDescriptor(final String descriptor) {
        this.descriptor = descriptor;
    }

}
