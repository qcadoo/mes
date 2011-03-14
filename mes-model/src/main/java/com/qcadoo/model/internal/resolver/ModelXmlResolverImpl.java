package com.qcadoo.model.internal.resolver;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.api.Constants;
import com.qcadoo.model.internal.api.ModelXmlResolver;

@Component
public final class ModelXmlResolverImpl implements ModelXmlResolver {

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public Resource[] getResources() {
        try {
            return resolver.getResources(Constants.RESOURCE_PATTERN);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find classpath resources for " + Constants.RESOURCE_PATTERN, e);
        }
    }

}
