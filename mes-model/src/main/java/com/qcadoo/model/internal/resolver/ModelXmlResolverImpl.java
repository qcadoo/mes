package com.qcadoo.model.internal.resolver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.module.ModelXmlHolder;
import com.qcadoo.model.internal.utils.JdomUtils;

@Component
public final class ModelXmlResolverImpl implements ModelXmlResolver, ModelXmlHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ModelXmlResolverImpl.class);

    private final Map<String, Document> documents = new HashMap<String, Document>();

    @Override
    public Resource[] getResources() {
        List<Resource> resources = new ArrayList<Resource>();

        for (Document document : documents.values()) {
            byte[] out = JdomUtils.documentToByteArray(document);
            if (LOG.isDebugEnabled()) {
                LOG.debug(new String(out));
            }
            resources.add(new ByteArrayResource(out));
        }

        documents.clear();

        return resources.toArray(new Resource[resources.size()]);
    }

    @Override
    public void put(final String pluginIdentifier, final String modelName, final InputStream stream) {
        Document document = JdomUtils.inputStreamToDocument(stream);
        document.getRootElement().setAttribute("plugin", pluginIdentifier);
        documents.put(pluginIdentifier + "." + modelName, document);
    }

    @Override
    public Document get(final String pluginIdentifier, final String modelName) {
        Document document = documents.get(pluginIdentifier + "." + modelName);
        checkNotNull(document, "Cannot find model for " + pluginIdentifier + "." + modelName);
        return document;
    }
}
