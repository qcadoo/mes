package com.qcadoo.model.internal.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.module.ModelXmlHolder;

@Component
public final class ModelXmlResolverImpl implements ModelXmlResolver, ModelXmlHolder {

    private final Map<String, Document> documents = new HashMap<String, Document>();

    @Override
    public Resource[] getResources() {
        try {
            List<Resource> resources = new ArrayList<Resource>();

            for (Document document : documents.values()) {
                XMLOutputter outputter = new XMLOutputter();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                outputter.output(document, out);
                resources.add(new ByteArrayResource(out.toByteArray()));
            }

            documents.clear();

            return resources.toArray(new Resource[resources.size()]);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void put(final String pluginIdentifier, final String modelName, final InputStream stream) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(stream);
            document.getRootElement().setAttribute("plugin", pluginIdentifier);
            documents.put(pluginIdentifier + "." + modelName, document);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Document get(final String pluginIdentifier, final String modelName) {
        return documents.get(pluginIdentifier + "." + modelName);
    }

}
