package com.qcadoo.mes.view.internal.module;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ViewTabModuleFactory implements ModuleFactory<ViewTabModule> {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Override
    public void init() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public ViewTabModule parse(final String pluginIdentifier, final Element element) {
        List<Resource> xmlFiles = new ArrayList<Resource>();
        for (Element resourceElement : (List<Element>) element.getChildren()) {
            String resource = resourceElement.getText();
            if (resource == null) {
                throw new IllegalStateException("Missing resource element of view module");
            }
            xmlFiles.add(new ClassPathResource(resource));
        }
        return new ViewTabModule(xmlFiles, viewDefinitionService, viewDefinitionParser);
    }

    @Override
    public String getIdentifier() {
        return "viewWindowTabExtension";
    }

}
