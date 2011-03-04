package com.qcadoo.model;

import java.io.File;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.qcadoo.model.internal.sessionfactory.DynamicSessionFactoryBean;

public class Utils {

    public static final String HBM_DTD_PATH = new File("src/test/resources/hibernate-mapping-3.0.dtd").getAbsolutePath();

    public static final String SPRING_CONTEXT_PATH = "spring.xml";

    public static final Resource FULL_XML_RESOURCE = new FileSystemResource("src/test/resources/full-model.xml");

    public static final Resource OTHER_XML_RESOURCE = new FileSystemResource("src/test/resources/other-model.xml");

    public static final Resource MODEL_XML_INVALID_RESOURCE = new FileSystemResource("src/test/resources/log4j.xml");

    public static final Resource FULL_HBM_RESOURCE = new FileSystemResource("src/test/resources/full.hbm.xml");

    public static final Resource EMPTY_HBM_RESOURCE = new FileSystemResource("src/test/resources/empty.hbm.xml");

    public static DataSource createDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:mes");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    public static DynamicSessionFactoryBean createNewSessionFactory() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        DynamicSessionFactoryBean sessionFactory = new DynamicSessionFactoryBean();
        sessionFactory.setDataSource(createDataSource());
        sessionFactory.setHibernateProperties(hibernateProperties);
        return sessionFactory;
    }

}
