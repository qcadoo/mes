package com.qcadoo.model.internal.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.qcadoo.model.internal.api.ModelXmlResolver;
import com.qcadoo.model.internal.module.ModelXmlHolder;

@Component
public final class ModelXmlResolverImpl implements ModelXmlResolver, ModelXmlHolder {

    private final Map<String, Document> documents = new HashMap<String, Document>();

    @Override
    public Resource[] getResources() {
        try {
            List<Resource> resources = new ArrayList<Resource>();

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            for (Document document : documents.values()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Result result = new StreamResult(out);
                Source source = new DOMSource(document);
                transformer.transform(source, result);

                resources.add(new ByteArrayResource(out.toByteArray()));
            }

            return resources.toArray(new Resource[resources.size()]);
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void put(final String pluginIdentifier, final String name, final InputStream stream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);

            ((Element) document.getChildNodes().item(0)).setAttribute("plugin", pluginIdentifier);

            documents.put(pluginIdentifier + "." + name, document);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public Document get(final String pluginIdentifier, final String name) {
        return documents.get(pluginIdentifier + "." + name);
    }

}
