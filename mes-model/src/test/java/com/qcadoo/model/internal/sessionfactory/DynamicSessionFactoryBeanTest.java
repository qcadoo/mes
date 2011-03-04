package com.qcadoo.model.internal.sessionfactory;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.Utils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;
import com.qcadoo.model.internal.api.ModelXmlToDefinitionConverter;
import com.qcadoo.model.internal.api.ModelXmlToHbmConverter;

public class DynamicSessionFactoryBeanTest {

    @Test
    public void shouldCreateSessionFactoryForValidModelXml() throws Exception {
        // given
        ModelXmlResolver modelXmlResolver = mock(ModelXmlResolver.class);
        given(modelXmlResolver.getResources()).willReturn(new Resource[] { Utils.FULL_XML_RESOURCE });

        List<InputStream> streams = Collections.singletonList(Utils.EMPTY_HBM_RESOURCE.getInputStream());
        ModelXmlToHbmConverter modelXmlToHbmConverter = mock(ModelXmlToHbmConverter.class);
        given(modelXmlToHbmConverter.convert(Utils.FULL_XML_RESOURCE)).willReturn(streams);

        List<Class<?>> classes = Collections.emptyList();
        ModelXmlToClassConverter modelXmlToClassConverter = mock(ModelXmlToClassConverter.class);
        given(modelXmlToClassConverter.convert(Utils.FULL_XML_RESOURCE)).willReturn(classes);

        List<DataDefinition> definitions = Collections.emptyList();
        ModelXmlToDefinitionConverter modelXmlToDefinitionConverter = mock(ModelXmlToDefinitionConverter.class);
        given(modelXmlToDefinitionConverter.convert(Utils.FULL_XML_RESOURCE)).willReturn(definitions);

        DynamicSessionFactoryBean sessionFactory = Utils.createNewSessionFactory();
        ReflectionTestUtils.setField(sessionFactory, "modelXmlResolver", modelXmlResolver);
        ReflectionTestUtils.setField(sessionFactory, "modelXmlToHbmConverter", modelXmlToHbmConverter);
        ReflectionTestUtils.setField(sessionFactory, "modelXmlToClassConverter", modelXmlToClassConverter);
        ReflectionTestUtils.setField(sessionFactory, "modelXmlToDefinitionConverter", modelXmlToDefinitionConverter);

        // when
        sessionFactory.afterPropertiesSet();

        // then
        verify(modelXmlToClassConverter, only()).convert(Utils.FULL_XML_RESOURCE);
        verify(modelXmlToHbmConverter, only()).convert(Utils.FULL_XML_RESOURCE);
        verify(modelXmlResolver, times(1)).getResources();
    }

}
