package com.qcadoo.model.internal.sessionfactory;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.TypeHelper;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;

public class DynamicSessionFactory implements SessionFactory {

    private static final long serialVersionUID = -7254335636932770807L;

    private final FactoryBean<SessionFactory> sessionFactoryBean;

    private SessionFactory sessionFactory;

    public DynamicSessionFactory(final FactoryBean<SessionFactory> sessionFactoryBean) {
        this.sessionFactoryBean = sessionFactoryBean;
    }

    private SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            System.out.println(" ---- 1");
            synchronized (this) {
                System.out.println(" ---- 2");
                if (sessionFactory == null) {
                    System.out.println(" ---- 3");
                    try {
                        sessionFactory = sessionFactoryBean.getObject();
                        System.out.println(" ---- 4 " + sessionFactoryBean.isSingleton());
                        System.out.println(" ---- 4 " + sessionFactoryBean.getObjectType());
                        System.out.println(" ---- 4 " + sessionFactoryBean.getObject());
                        System.out.println(" ---- 4 " + sessionFactory);

                        if (sessionFactory == null) {
                            throw new IllegalStateException();
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
            }
        }
        return sessionFactory;
    }

    @Override
    public Reference getReference() throws NamingException {
        return getSessionFactory().getReference();
    }

    @Override
    public Session openSession() throws HibernateException {
        return getSessionFactory().openSession();
    }

    @Override
    public Session openSession(final Interceptor interceptor) throws HibernateException {
        return getSessionFactory().openSession(interceptor);
    }

    @Override
    public Session openSession(final Connection connection) {
        return getSessionFactory().openSession(connection);
    }

    @Override
    public Session openSession(final Connection connection, final Interceptor interceptor) {
        return getSessionFactory().openSession(connection, interceptor);
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return getSessionFactory().getCurrentSession();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return getSessionFactory().openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(final Connection connection) {
        return getSessionFactory().openStatelessSession(connection);
    }

    @Override
    public ClassMetadata getClassMetadata(final Class entityClass) {
        return getSessionFactory().getClassMetadata(entityClass);
    }

    @Override
    public ClassMetadata getClassMetadata(final String entityName) {
        return getSessionFactory().getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(final String roleName) {
        return getSessionFactory().getCollectionMetadata(roleName);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return getSessionFactory().getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() {
        return getSessionFactory().getAllCollectionMetadata();
    }

    @Override
    public Statistics getStatistics() {
        return getSessionFactory().getStatistics();
    }

    @Override
    public void close() throws HibernateException {
        getSessionFactory().close();
    }

    @Override
    public boolean isClosed() {
        return getSessionFactory().isClosed();
    }

    @Override
    public Cache getCache() {
        return getSessionFactory().getCache();
    }

    @Override
    public void evict(final Class persistentClass) throws HibernateException {
        getSessionFactory().evict(persistentClass);
    }

    @Override
    public void evict(final Class persistentClass, final Serializable id) throws HibernateException {
        getSessionFactory().evict(persistentClass, id);
    }

    @Override
    public void evictEntity(final String entityName) throws HibernateException {
        getSessionFactory().evictEntity(entityName);
    }

    @Override
    public void evictEntity(final String entityName, final Serializable id) throws HibernateException {
        getSessionFactory().evictEntity(entityName, id);
    }

    @Override
    public void evictCollection(final String roleName) throws HibernateException {
        getSessionFactory().evictCollection(roleName);
    }

    @Override
    public void evictCollection(final String roleName, final Serializable id) throws HibernateException {
        getSessionFactory().evictCollection(roleName, id);
    }

    @Override
    public void evictQueries(final String cacheRegion) throws HibernateException {
        getSessionFactory().evictQueries(cacheRegion);
    }

    @Override
    public void evictQueries() throws HibernateException {
        getSessionFactory().evictQueries();
    }

    @Override
    public Set getDefinedFilterNames() {
        return getSessionFactory().getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(final String filterName) throws HibernateException {
        return getSessionFactory().getFilterDefinition(filterName);
    }

    @Override
    public boolean containsFetchProfileDefinition(final String name) {
        return getSessionFactory().containsFetchProfileDefinition(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return getSessionFactory().getTypeHelper();
    }

}
