package com.qcadoo.mes.assignmentToShift;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.Module;

@Component
public class OccupationTypeLoaderModule extends Module {

    protected static final Logger LOG = LoggerFactory.getLogger(OccupationTypeLoaderModule.class);

    private static final String L_DICTIONARY = "dictionary";

    private static final String L_TECHNICAL_CODE = "technicalCode";

    private static final String L_NAME = "name";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public final void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Currency table will be populated...");
            }
            readDataFromXML();
        }
    }

    private void readDataFromXML() {
        LOG.info("Loading test data from occupationType.xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getCurrencyXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> nodes = rootNode.getChildren("row");
            for (Element node : nodes) {
                parseAndAddDictionaries(node);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void parseAndAddDictionaries(final Element node) {
        @SuppressWarnings("unchecked")
        List<Attribute> attributes = node.getAttributes();
        Map<String, String> values = new HashMap<String, String>();
        for (Attribute attribute : attributes) {
            values.put(attribute.getName().toLowerCase(Locale.getDefault()), attribute.getValue());
        }
        addDictionaryItem(values);
    }

    private void addDictionaryItem(final Map<String, String> values) {
        DataDefinition dictionaryItemDataDefinition = getDictionaryItemDataDefinition();
        Entity dictionaryItem = dictionaryItemDataDefinition.create();

        dictionaryItem.setField(L_TECHNICAL_CODE, values.get(L_TECHNICAL_CODE.toLowerCase(Locale.getDefault())));
        dictionaryItem.setField(L_NAME, values.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(L_DICTIONARY, getlDictionary());

        dictionaryItem = dictionaryItemDataDefinition.save(dictionaryItem);
        if (dictionaryItem.isValid() && LOG.isDebugEnabled()) {
            LOG.debug("Currency saved {currency=" + dictionaryItem.toString() + "}");
        } else {
            throw new IllegalStateException("Saved dictionaries entity have validation errors - " + values.get(L_NAME));
        }
    }

    private boolean databaseHasToBePrepared() {
        return getDictionaryItemDataDefinition()
                .find()
                .add(SearchRestrictions.or(SearchRestrictions.eq("technicalCode", "01workForLine"),
                        SearchRestrictions.eq("technicalCode", "02otherCase"))).list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getDictionaryItemDataDefinition() {
        return dataDefinitionService.get("qcadooModel", "dictionaryItem");
    }

    private Entity getlDictionary() {
        return dataDefinitionService.get("qcadooModel", L_DICTIONARY).find().add(SearchRestrictions.eq(L_NAME, "occupationType"))
                .uniqueResult();
    }

    private InputStream getCurrencyXmlFile() throws IOException {
        return OccupationTypeLoaderModule.class.getResourceAsStream("/assignmentToShift/model/data/occupationType" + "_"
                + Locale.getDefault().getLanguage() + ".xml");
    }

}
