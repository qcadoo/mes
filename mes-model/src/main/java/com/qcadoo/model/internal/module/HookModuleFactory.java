package com.qcadoo.model.internal.module;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.model.internal.utils.JdomUtils;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class HookModuleFactory implements ModuleFactory<HookModule> {

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
    public HookModule parse(final String pluginIdentifier, final Element element) {
        String targetPluginIdentifier = element.getAttributeValue("plugin");
        String targetModelName = element.getAttributeValue("model");

        if (targetPluginIdentifier == null) {
            throw new IllegalStateException("Missing plugin attribute of hook module");
        }

        if (targetModelName == null) {
            throw new IllegalStateException("Missing model attribute of hook module");
        }

        List<Element> elements = element.getChildren();

        if (elements.size() < 1) {
            throw new IllegalStateException("Missing content of hook module");
        } else if (elements.size() > 1) {
            throw new IllegalStateException("Only one hook can be defined in single hook module");
        }

        Document document = modelXmlHolder.get(targetPluginIdentifier, targetModelName);

        Element hook = JdomUtils.replaceNamespace(elements.get(0), document.getRootElement().getNamespace());

        String hookType = hook.getName();
        String hookClass = hook.getAttributeValue("class");
        String hookMethod = hook.getAttributeValue("method");

        Element hooks = (Element) document.getRootElement().getChildren().get(1);

        if (!"hooks".equals(hooks.getName())) {
            throw new IllegalStateException("Expected element hooks, found " + hooks.getName());
        }

        hooks.addContent(hook.detach());

        return new HookModule(targetPluginIdentifier, targetModelName, hookType, hookClass, hookMethod, dataDefinitionService);
    }

    @Override
    public String getIdentifier() {
        return "hook";
    }

}
