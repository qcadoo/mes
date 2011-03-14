package com.qcadoo.plugin.internal.descriptorresolver;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorResolver implements PluginDescriptorResolver {

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    // @Value("#{ $qcadoo.plugin.restartCommand != null ? $qcadoo.plugin.restartCommand : 'plugin.xml' }")
    private String descriptor = "com/qcadoo/plugin/integration/*/plugin.xml";

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
