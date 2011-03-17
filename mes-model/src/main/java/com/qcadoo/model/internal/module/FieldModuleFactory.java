package com.qcadoo.model.internal.module;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class FieldModuleFactory implements ModuleFactory<FieldModule> {

    @Autowired
    private ModelXmlHolder modelXmlHolder;

    @Autowired
    private InternalDataDefinitionService dataDefinitionService;

    @Override
    public void postInitialize() {
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

        System.out.println(" 1 ---------> " + targetPluginIdentifier + "." + targetModelName);

        List<Element> elements = element.getChildren();

        System.out.println(" 2 ---------> " + elements.size());

        if (elements.size() < 1) {
            throw new IllegalStateException("Missing content of field module");
        } else if (elements.size() > 1) {
            throw new IllegalStateException("Only one field can be defined in single field module");
        }

        String fieldName = elements.get(0).getAttributeValue("name");

        System.out.println(" 3 ---------> " + fieldName);

        Document document = modelXmlHolder.get(targetPluginIdentifier, targetModelName);

        System.out.println(" 4 ---------> " + document);
        System.out.println(" 5 ---------> " + document.getRootElement());
        System.out.println(" 6 ---------> " + document.getRootElement().getChildren().size());
        System.out.println(" 7 ---------> " + document.getRootElement().getChildren().get(0));
        System.out.println(" 8 ---------> " + document.getRootElement().getChildren().get(1));

        ((Element) document.getRootElement().getChildren("fields").get(0)).addContent(elements.get(0));

        return new FieldModule(targetPluginIdentifier, targetModelName, fieldName, dataDefinitionService);
    }

    @Override
    public String getIdentifier() {
        return "field";
    }

}
