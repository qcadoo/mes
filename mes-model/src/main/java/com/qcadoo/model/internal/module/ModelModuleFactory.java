package com.qcadoo.model.internal.module;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.qcadoo.model.internal.api.DynamicSessionFactoryBean;
import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;
import com.qcadoo.model.internal.api.ModelXmlToDefinitionConverter;
import com.qcadoo.model.internal.api.ModelXmlToHbmConverter;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ModelModuleFactory implements ModuleFactory<ModelModule> {

    @Autowired
    private ModelXmlToHbmConverter modelXmlToHbmConverter;

    @Autowired
    private ModelXmlToClassConverter modelXmlToClassConverter;

    @Autowired
    private ModelXmlToDefinitionConverter modelXmlToDefinitionConverter;

    @Autowired
    private ModelXmlHolder modelXmlHolder;

    @Autowired
    private ModelXmlResolver modelXmlResolver;

    @Autowired
    private InternalDataDefinitionService dataDefinitionService;

    @Autowired
    private DynamicSessionFactoryBean sessionFactoryBean;

    @Override
    public void postInitialize() {
        Resource[] resources = modelXmlResolver.getResources();

        modelXmlToClassConverter.convert(resources);

        sessionFactoryBean.initialize(modelXmlToHbmConverter.convert(resources));

        modelXmlToDefinitionConverter.convert(resources);
    }

    @Override
    public ModelModule parse(final String pluginIdentifier, final Element element) {
        String modelName = element.getAttributeValue("model");

        if (modelName == null) {
            throw new IllegalStateException("Missing model attribute of model module");
        }

        String content = element.getText();

        String resource = element.getAttributeValue("resource");

        if (resource == null && !StringUtils.hasText(content)) {
            throw new IllegalStateException("Missing resource attribute or content of model module");
        }

        if (resource != null) {
            try {
                modelXmlHolder.put(pluginIdentifier, modelName, new ClassPathResource(resource).getInputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } else {
            modelXmlHolder.put(pluginIdentifier, modelName, new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        }

        return new ModelModule(pluginIdentifier, modelName, dataDefinitionService);

    }

    @Override
    public String getIdentifier() {
        return "model";
    }

}
