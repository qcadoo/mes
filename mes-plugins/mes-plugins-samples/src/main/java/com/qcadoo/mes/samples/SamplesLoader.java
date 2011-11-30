package com.qcadoo.mes.samples;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginAccessor;

public abstract class SamplesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(SamplesLoader.class);

    @Autowired
    private PluginAccessor pluginAccessor;

    abstract void loadData(final String dataset, final String locale);

    void readData(final Map<String, String> values, final String type, final Element node) {
        // default read data implementation
    }

    void readDataFromXML(final String dataset, final String object, final String locale) {

        LOG.info("Loading test data from " + object + "_" + locale + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(dataset, object, locale));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {
                Element node = list.get(i);
                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtribute = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();

                for (int j = 0; j < listOfAtribute.size(); j++) {
                    values.put(listOfAtribute.get(j).getName().toLowerCase(), listOfAtribute.get(j).getValue());
                }
                readData(values, object, node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    boolean isEnabled(final String pluginIdentifier) {
        return pluginAccessor.getPlugin(pluginIdentifier) != null;
    }

    void validateEntity(final Entity entity) {
        if (!entity.isValid()) {
            Map<String, ErrorMessage> errors = entity.getErrors();
            Set<String> keys = errors.keySet();
            StringBuilder stringError = new StringBuilder("Saved entity is invalid\n");
            for (String key : keys) {
                stringError.append("\t").append(key).append("  -  ").append(errors.get(key).getMessage()).append("\n");
            }
            Map<String, Object> fields = entity.getFields();
            for (Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    stringError.append("\t\t");
                }
                stringError.append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
            }
            throw new IllegalStateException("Saved entity is invalid\n" + stringError.toString());
        }
    }

    private InputStream getXmlFile(final String dataset, final String object, final String locale) throws IOException {
        return TestSamplesLoader.class.getResourceAsStream("/com/qcadoo/mes/samples/" + dataset + "/" + object + "_" + locale
                + ".xml");
    }

}
