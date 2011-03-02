package com.qcadoo.model;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.qcadoo.mes.api.DataDefinitionService;

public class HibernateMain {

    public static void main(final String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");

        // when
        DataDefinitionService entityContext = applicationContext.getBean(DataDefinitionService.class);

        // System.out.println(entityContext.get("", "firstEntity").getEntityName());

        // Entity employee = entityContext.get("").create();
        // employee.setField("fsdfsd", "dssdf");

        // System.out.println(" --- creating --- ");
        //
        // Entity employee = new EntityImpl("employee");
        // employee.setField("firstName", "Maciek");
        // employee.setField("nickName", "Wielki");
        //
        // Entity employer = new EntityImpl("employer");
        // employer.setField("name", "Bardzodlugaglupianazwafirmy");
        //
        // Serializable employeeId = session.save(employee);
        // Serializable employerId = session.save(employer);
        // session.flush();
        // session.clear();
        //
        // System.out.println(" --- saved --- ");
        //
        // employee = (Entity) session.get("plugin.employee", employeeId);
        // employer = (Entity) session.get("plugin.employer", employerId);
        //
        // System.out.println(employee);
        // System.out.println(employee.getFields());
        // System.out.println(employer);
        // System.out.println(employer.getFields());
        //
        // System.out.println(" --- deleting --- ");
        //
        // session.delete(employee);
        // session.delete(employer);
        // session.flush();
        // session.clear();
        //
        // System.out.println(" --- deleted --- ");
        //
        // employee = (Entity) session.get("group.firstEntity", employeeId);
        // employer = (Entity) session.get("group.secondEntity", employeeId);
        //
        // System.out.println(employee);
        // System.out.println(employer);
        //
        // session.close();
    }

    // private EntityContext getEntityContext() throws Exception {
    // Properties hibernateProperties = new Properties();
    // hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    // hibernateProperties.setProperty("hibernate.show_sql", "true");
    // hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
    // QcadooOrmContext entityContext = new QcadooOrmContext();
    // entityContext.setDataSource(getDataSource());
    // entityContext.setHibernateProperties(hibernateProperties);
    // entityContext.afterPropertiesSet();

    // System.out.println(" ---------- ");
    //
    // DatabaseMetadata databaseMetadata = new DatabaseMetadata(sessionFactory.getObject().openSession().connection(),
    // new HSQLDialect());
    //
    // String[] ddl = sessionFactory.getConfiguration().generateSchemaUpdateScript(new HSQLDialect(), databaseMetadata);
    // for (String line : ddl) {
    // DDLFormatterImpl formatter = new DDLFormatterImpl();
    // line = formatter.format(line);
    // System.out.println(line);
    // }
    //
    // System.out.println(" ---------- ");

    // SchemaExport schemaExport = new SchemaExport(sessionFactory.getConfiguration(),
    // sessionFactory.getObject().openSession()
    // .connection());
    // schemaExport.setDelimiter(";");
    // boolean script = true;
    // boolean justCreate = true;
    // boolean justDrop = false;
    // boolean export = true;
    // schemaExport.execute(script, export, justDrop, justCreate);

    // System.out.println(" ---------- ");

    // return entityContext;
    // }

}
