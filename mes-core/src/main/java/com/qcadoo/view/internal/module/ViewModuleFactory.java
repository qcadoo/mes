package com.qcadoo.view.internal.module;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.ModuleFactory;
import com.qcadoo.view.api.InternalViewDefinitionService;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

public class ViewModuleFactory implements ModuleFactory<ViewModule> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private InternalViewDefinitionService viewDefinitionService;

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
