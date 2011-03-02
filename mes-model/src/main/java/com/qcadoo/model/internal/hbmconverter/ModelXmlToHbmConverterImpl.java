package com.qcadoo.model.internal.hbmconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.qcadoo.model.internal.api.Constants;
import com.qcadoo.model.internal.api.ModelXmlToHbmConverter;

@Component
public class ModelXmlToHbmConverterImpl implements ModelXmlToHbmConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ModelXmlToHbmConverterImpl.class);

    private final Resource xsl = new ClassPathResource(Constants.XSL);

    private final Transformer transformer;

    public ModelXmlToHbmConverterImpl() {
        if (!xsl.isReadable()) {
            throw new IllegalStateException("Failed to read " + xsl.getFilename());
        }
        try {
            transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl.getInputStream()));
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Failed to initialize xsl transformer", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize xsl transformer", e);
        }
    }

    @Override
    public Collection<InputStream> convert(final Resource... resources) {
        try {
            List<InputStream> streams = new ArrayList<InputStream>();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    LOG.info("Converting " + resource.getURI().toString() + " to hbm.xml");

                    ByteArrayOutputStream hbm = new ByteArrayOutputStream();

                    transformer.transform(new StreamSource(resource.getInputStream()), new StreamResult(hbm));

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(new String(hbm.toByteArray()));
                    }

                    streams.add(new ByteArrayInputStream(hbm.toByteArray()));
                }
            }

            return streams;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert model.xml to hbm.xml", e);
        } catch (TransformerException e) {
            throw new IllegalStateException("Failed to convert model.xml to hbm.xml", e);
        }
    }
}
