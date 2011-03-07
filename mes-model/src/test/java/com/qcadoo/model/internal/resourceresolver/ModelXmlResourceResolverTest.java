package com.qcadoo.model.internal.resourceresolver;

import static org.junit.Assert.assertEquals;

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
        assertEquals(4, resources.length);
        assertEquals("full.xml", resources[0].getFilename());
        assertEquals("integration.xml", resources[1].getFilename());
        assertEquals("other.xml", resources[2].getFilename());
        assertEquals("dictionaries.xml", resources[3].getFilename());
    }

}
