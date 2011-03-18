package com.qcadoo.model.internal.module;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.model.internal.utils.JdomUtils;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class FieldModuleFactory implements ModuleFactory<FieldModule> {

    @Autowired
    private ModelXmlHolder modelXmlHolder;

    @Autowired
    private InternalDataDefinitionService dataDefinitionService;

    @Override
    public void init() {
        // empty
    }

    @Override
    @SuppressWarnings("unchecked")
    public FieldModule parse(final String pluginIdentifier, final Element element) {
        String targetPluginIdentifier = element.getAttributeValue("plugin");
        String targetModelName = element.getAttributeValue("model");

        if (targetPluginIdentifier == null) {
            throw new IllegalStateException("Missing plugin attribute of field module");
        }

        if (targetModelName == null) {
            throw new IllegalStateException("Missing model attribute of field module");
        }

        List<Element> elements = element.getChildren();

        if (elements.size() < 1) {
            throw new IllegalStateException("Missing content of field module");
        } else if (elements.size() > 1) {
            throw new IllegalStateException("Only one field can be defined in single field module");
        }

        Document document = modelXmlHolder.get(targetPluginIdentifier, targetModelName);

        Element field = JdomUtils.replaceNamespace(elements.get(0), document.getRootElement().getNamespace());

        String fieldName = field.getAttributeValue("name");

        Element fields = (Element) document.getRootElement().getChildren().get(0);

        if (!"fields".equals(fields.getName())) {
            throw new IllegalStateException("Expected element fields, found " + fields.getName());
        }

        fields.addContent(field.detach());

        return new FieldModule(targetPluginIdentifier, targetModelName, fieldName, dataDefinitionService);
    }

    @Override
    public String getIdentifier() {
        return "field";
    }

}
