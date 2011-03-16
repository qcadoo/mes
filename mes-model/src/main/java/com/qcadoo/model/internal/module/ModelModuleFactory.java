package com.qcadoo.model.internal.module;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.internal.api.DynamicSessionFactoryBean;
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
    private DataDefinitionService dataDefinitionService;

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
    public ModelModule parse(final String pluginIdentifier, final Node node) {
        Node nodeName = node.getAttributes().getNamedItem("name");

        if (nodeName == null) {
            throw new IllegalStateException("Missing name attribute of model module");
        }

        String content = node.getTextContent();

        Node resourceName = node.getAttributes().getNamedItem("resource");

        if (resourceName == null && !StringUtils.hasText(content)) {
            throw new IllegalStateException("Missing resource attribute or text content of model module");
        }

        String name = nodeName.getNodeValue();

        if (resourceName != null) {
            modelXmlHolder.put(pluginIdentifier, name, ClassLoader.getSystemResourceAsStream(resourceName.getNodeValue()));
        } else {
            modelXmlHolder.put(pluginIdentifier, name, new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        }

        return new ModelModule(pluginIdentifier, name, dataDefinitionService);

    }

    @Override
    public String getIdentifier() {
        return "model";
    }

}
