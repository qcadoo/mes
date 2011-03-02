package com.qcadoo.model;

import java.io.File;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.core.io.ClassPathResource;

import com.qcadoo.model.internal.sessionfactory.DynamicSessionFactoryBean;

public class Utils {

    public static final String HBM_DTD_PATH = new File("src/test/resources/hibernate-mapping-3.0.dtd").getAbsolutePath();

    public static final String SPRING_CONTEXT_PATH = "spring.xml";

    public static final ClassPathResource MODEL_XML_RESOURCE = new ClassPathResource("full-model.xml");

    public static final ClassPathResource OTHER_XML_RESOURCE = new ClassPathResource("other-model.xml");

    public static final ClassPathResource MODEL_XML_INVALID_RESOURCE = new ClassPathResource("log4j.xml");

    public static final ClassPathResource HBM_RESOURCE = new ClassPathResource("full.hbm.xml");

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
