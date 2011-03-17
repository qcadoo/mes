package com.qcadoo.model.internal.resourceresolver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;
import org.springframework.core.io.Resource;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.module.ModelXmlHolder;
import com.qcadoo.model.internal.resolver.ModelXmlResolverImpl;

public class ModelXmlResourceResolverTest {

    @Test
    public void shouldReturnAllMatchingResources() throws Exception {
        // given
        ModelXmlResolver resourceResolver = new ModelXmlResolverImpl();
        ((ModelXmlHolder) resourceResolver).put("full", "firstEntity", new FileInputStream(new File(
                "src/test/resources/model/full/firstEntity.xml")));

        // when
        Resource[] resources = resourceResolver.getResources();

        // then
        assertEquals(1, resources.length);
    }

}
