package com.qcadoo.view.internal.module;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.ModuleFactory;
import com.qcadoo.view.internal.api.InternalViewDefinitionService;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

public class ViewRibbonModuleFactory implements ModuleFactory<ViewRibbonModule> {

    @Autowired
    private InternalViewDefinitionService viewDefinitionService;

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Override
    public void init() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public ViewRibbonModule parse(final String pluginIdentifier, final Element element) {
        List<Resource> xmlFiles = new ArrayList<Resource>();
        for (Element resourceElement : (List<Element>) element.getChildren()) {
            String resource = resourceElement.getText();
            if (resource == null) {
                throw new IllegalStateException("Missing resource element of view module");
            }
            xmlFiles.add(new ClassPathResource(resource));
        }

        return new ViewRibbonModule(xmlFiles, viewDefinitionService, viewDefinitionParser);
    }

    @Override
    public String getIdentifier() {
        return "viewRibbonExtension";
    }

}
