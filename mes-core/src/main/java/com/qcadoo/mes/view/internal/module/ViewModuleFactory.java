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

public class ViewModuleFactory implements ModuleFactory<ViewModule> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Override
    public void init() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public ViewModule parse(final String pluginIdentifier, final Element element) {
        List<Resource> xmlFiles = new ArrayList<Resource>();
        for (Element resourceElement : (List<Element>) element.getChildren()) {
            String resource = resourceElement.getText();
            if (resource == null) {
                throw new IllegalStateException("Missing resource element of view module");
            }
            xmlFiles.add(new ClassPathResource(resource));
        }
        return new ViewModule(xmlFiles, viewDefinitionParser, viewDefinitionService);

    }

    @Override
    public String getIdentifier() {
        return "view";
    }

}
