package com.qcadoo.model.internal.resourceresolver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.core.io.Resource;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.resolver.ModelXmlResolverImpl;

public class ModelXmlResourceResolverTest {

    @Test
    public void shouldReturnAllMatchingResources() throws Exception {
        // given
        ModelXmlResolver resourceResolver = new ModelXmlResolverImpl();

        // when
        Resource[] resources = resourceResolver.getResources();

        // then
        assertTrue(resources.length > 0);

        for (Resource resource : resources) {
            assertTrue(resource.getFilename().endsWith("-model.xml"));
        }
    }

}
