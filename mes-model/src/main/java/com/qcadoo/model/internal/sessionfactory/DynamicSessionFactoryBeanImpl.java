package com.qcadoo.model.internal.sessionfactory;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import com.qcadoo.model.internal.api.DynamicSessionFactoryBean;

public final class DynamicSessionFactoryBeanImpl implements DynamicSessionFactoryBean {

    private final LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();

    @Override
    public void afterPropertiesSet() throws Exception {
        // ignore
    }

    @Override
    public void initialize(final Resource[] hbms) {
        factoryBean.setMappingLocations(hbms);

        try {
            factoryBean.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        factoryBean.setBeanClassLoader(classLoader);
    }

    @Override
    public SessionFactory getObject() throws Exception {
        return new DynamicSessionFactory(factoryBean);
    }

    @Override
    public Class<?> getObjectType() {
        return factoryBean.getObjectType();
    }

    @Override
    public boolean isSingleton() {
        return factoryBean.isSingleton();
    }

    @Override
    public void destroy() throws Exception {
        factoryBean.destroy();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(final RuntimeException ex) {
        return factoryBean.translateExceptionIfPossible(ex);
    }

    public void setDataSource(final DataSource dataSource) {
        factoryBean.setDataSource(dataSource);
    }

    public void setHibernateProperties(final Properties hibernateProperties) {
        factoryBean.setHibernateProperties(hibernateProperties);
    }

}
