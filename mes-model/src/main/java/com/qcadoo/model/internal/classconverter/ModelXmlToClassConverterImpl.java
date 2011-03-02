package com.qcadoo.model.internal.classconverter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.api.ModelXmlToClassConverter;

@Component
public class ModelXmlToClassConverterImpl implements ModelXmlToClassConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ModelXmlToClassConverterImpl.class);

    @Override
    public Collection<Class<?>> convert(final Resource... resources) {
        try {
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Converting " + resource.getURI().toString() + " to classes");

                }
            }

            return Collections.emptyList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert model.xml to classes", e);
        }
    }

    // @Override
    // protected void postProcessMappings(final Configuration config) throws HibernateException {
    // try {
    // // Resource resource = new ClassPathResource(mapping.trim(), this.beanClassLoader);
    // // config.addInputStream(resource.getInputStream());
    //
    // ClassPool pool = ClassPool.getDefault();
    // CtClass bean1 = pool.makeClass("co.co.co.Bean1");
    //
    // // CtClass bean2 = pool.makeClass("Bean2");
    //
    // bean1.addField(CtField.make("private Long id;", bean1));
    // bean1.addField(CtField.make("private String name;", bean1));
    // bean1.addMethod(CtNewMethod.make("public String getName() { return name; }", bean1));
    // bean1.addMethod(CtNewMethod.make("public Long getId() { return id; }", bean1));
    // bean1.addMethod(CtNewMethod.make("public void setId(Long id) { this.id = id; }", bean1));
    // bean1.addMethod(CtNewMethod.make("public void setName(String name) { this.name = name; }", bean1));
    //
    // // bean2.addField(CtField.make("private String name = \"Nazwa\";", bean2));
    // // bean2.addField(CtField.make("private java.util.List beans = new java.util.ArrayList();", bean2));
    // // bean2.addMethod(CtNewMethod.make("public String getName() { return name; }", bean2));
    // // bean2.addMethod(CtNewMethod.make("public java.util.List getBeans() { return beans; }", bean2));
    // // bean2.addMethod(CtNewMethod.make("public void addBeans(Object bean) { beans.add(bean); }", bean2));
    //
    // // bean1.addField(CtField.make("private Bean2 bean;", bean1));
    // // bean1.addMethod(CtNewMethod.make("public Bean2 getBean() { return bean; }", bean1));
    // // bean1.addMethod(CtNewMethod.make("public void setBean(Bean2 bean) { this.bean = bean; }", bean1));
    // // bean1.addMethod(CtNewMethod.make("public String getName() { return bean.getName(); }", bean1));
    //
    // System.out.println(bean1.toClass());
    // } catch (Exception e) {
    // throw new IllegalStateException(e.getMessage(), e);
    // }
    //
    // super.postProcessMappings(config);
    // }

}
