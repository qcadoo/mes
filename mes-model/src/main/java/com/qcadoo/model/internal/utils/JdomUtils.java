package com.qcadoo.model.internal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class JdomUtils {

    @SuppressWarnings("unchecked")
    public static Element replaceNamespace(final Element element, final Namespace namespace) {
        element.setNamespace(namespace);

        for (Element child : (List<Element>) element.getChildren()) {
            replaceNamespace(child, namespace);
        }

        return element;
    }

    public static byte[] documentToByteArray(final Document document) {
        try {
            XMLOutputter outputter = new XMLOutputter();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            outputter.output(document, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Document inputStreamToDocument(final InputStream stream) {
        try {
            SAXBuilder builder = new SAXBuilder();
            return builder.build(stream);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
