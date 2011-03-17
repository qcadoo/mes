package com.qcadoo.model.internal.api;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.support.PersistenceExceptionTranslator;

public interface DynamicSessionFactoryBean extends BeanClassLoaderAware, FactoryBean<SessionFactory>, InitializingBean,
        DisposableBean, PersistenceExceptionTranslator {

    void initialize(Resource[] hbms);

}
