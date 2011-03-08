package com.qcadoo.model.integration;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.internal.api.ModelXmlToDefinitionConverter;

public abstract class IntegrationTest {

    protected static String PLUGIN_NAME = "products";

    protected static String ENTITY_NAME_PRODUCT = "product";

    protected static String ENTITY_NAME_MACHINE = "machine";

    protected static String ENTITY_NAME_COMPONENT = "component";

    protected static String TABLE_NAME_PRODUCT = PLUGIN_NAME + "_" + ENTITY_NAME_PRODUCT;

    protected static String TABLE_NAME_MACHINE = PLUGIN_NAME + "_" + ENTITY_NAME_MACHINE;

    protected static String TABLE_NAME_COMPONENT = PLUGIN_NAME + "_" + ENTITY_NAME_COMPONENT;

    protected static DataDefinitionService dataDefinitionService;

    protected static SessionFactory sessionFactory;

    protected static JdbcTemplate jdbcTemplate;

    protected static ApplicationContext applicationContext;

    @BeforeClass
    public static void classInit() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        dataDefinitionService = applicationContext.getBean(DataDefinitionService.class);
        sessionFactory = applicationContext.getBean(SessionFactory.class);
        jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);

        (applicationContext.getBean(ModelXmlToDefinitionConverter.class)).onApplicationEvent(null);
    }

    @Before
    public void init() throws Exception {
        jdbcTemplate.execute("delete from " + TABLE_NAME_COMPONENT);
        jdbcTemplate.execute("delete from " + TABLE_NAME_MACHINE);
        jdbcTemplate.execute("delete from " + TABLE_NAME_PRODUCT);
    }

    protected Entity createComponent(final String name, final Object product, final Object machine) {
        Entity entity = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_COMPONENT).create();
        entity.setField("name", name);
        entity.setField("product", product);
        entity.setField("machine", machine);
        return entity;
    }

    protected Entity createMachine(final String name) {
        Entity entity = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_MACHINE).create();
        entity.setField("name", name);
        return entity;
    }

    protected Entity createProduct(final String name, final String number) {
        Entity entity = dataDefinitionService.get(PLUGIN_NAME, ENTITY_NAME_PRODUCT).create();
        entity.setField("name", name);
        entity.setField("number", number);
        return entity;
    }

}
