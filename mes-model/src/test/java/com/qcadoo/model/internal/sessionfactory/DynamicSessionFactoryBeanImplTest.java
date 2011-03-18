package com.qcadoo.model.internal.sessionfactory;

import org.junit.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.qcadoo.model.Utils;

public class DynamicSessionFactoryBeanImplTest {

    @Test
    public void shouldCreateSessionFactoryForValidModelXml() throws Exception {
        // given
        DynamicSessionFactoryBeanImpl sessionFactory = Utils.createNewSessionFactory();
        sessionFactory.initialize(new Resource[] { new InputStreamResource(Utils.EMPTY_HBM_RESOURCE.getInputStream()) });

        // when
        sessionFactory.afterPropertiesSet();
    }

}
