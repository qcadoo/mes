package com.qcadoo.model.internal.sessionfactory;

import java.io.InputStream;

import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;
import com.qcadoo.model.internal.api.ModelXmlToHbmConverter;

public final class DynamicSessionFactoryBean extends LocalSessionFactoryBean {

    @Autowired
    private ModelXmlToHbmConverter modelXmlToHbmConverter;

    @Autowired
    private ModelXmlToClassConverter modelXmlToClassConverter;

    @Autowired
    private ModelXmlResolver modelXmlResolver;

    @Override
    protected void postProcessMappings(final Configuration config) {
        Resource[] resources = modelXmlResolver.getResources();

        modelXmlToClassConverter.convert(resources);

        for (InputStream stream : modelXmlToHbmConverter.convert(resources)) {
            config.addInputStream(stream);
        }
    }

}
