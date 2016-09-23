/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic;

import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.in;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.plugins.dictionaries.DictionariesService;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class AddressTypeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(AddressTypeLoader.class);

    private static final String L_NAME = "name";

    private static final String L_TECHNICAL_CODE = "technicalCode";

    private static final String L_ADDRESS_TYPE = "addressType";

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DictionariesService dictionariesService;

    public final void loadAddressTypes() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Address types will be populated ...");
            }

            Map<Integer, Map<String, String>> addressTypesAttributes = getAddressTypesAttributesFromXML();

            addressTypesAttributes.values().forEach(addressTypeAttributes -> addDictionaryItem(addressTypeAttributes));
        }
    }

    public void unloadAddressTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Address types will be unpopulated ...");
        }

        Map<Integer, Map<String, String>> addressTypesAttributes = getAddressTypesAttributesFromXML();

        List<String> technicalCodes = addressTypesAttributes.values().stream()
                .map(addressTypeAttributes -> addressTypeAttributes.get(L_TECHNICAL_CODE)).filter(Objects::nonNull)
                .collect(Collectors.toList());

        deactivateDictionaryItems(technicalCodes);
    }

    private Map<Integer, Map<String, String>> getAddressTypesAttributesFromXML() {
        LOG.info("Loading data from addressType" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml ...");

        Map<Integer, Map<String, String>> addressTypesAttributes = Maps.newHashMap();

        try {
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(getAddressTypeXmlFile());
            Element rootNode = document.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> listOfRows = rootNode.getChildren("row");

            for (int rowNum = 0; rowNum < listOfRows.size(); rowNum++) {
                parseAndAddAddressType(addressTypesAttributes, listOfRows, rowNum);
            }
        } catch (IOException | JDOMException e) {
            LOG.error(e.getMessage(), e);
        }

        return addressTypesAttributes;
    }

    private void parseAndAddAddressType(final Map<Integer, Map<String, String>> addressTypesAttributes,
            final List<Element> listOfRows, final int rowNum) {
        Element node = listOfRows.get(rowNum);

        @SuppressWarnings("unchecked")
        List<Attribute> listOfAtributes = node.getAttributes();

        Map<String, String> addressTypeAttributes = Maps.newHashMap();

        for (int attributeNum = 0; attributeNum < listOfAtributes.size(); attributeNum++) {
            addressTypeAttributes.put(listOfAtributes.get(attributeNum).getName().toLowerCase(Locale.ENGLISH), listOfAtributes
                    .get(attributeNum).getValue());
        }

        addressTypesAttributes.put(rowNum, addressTypeAttributes);
    }

    private void addDictionaryItem(final Map<String, String> addressTypeAttributes) {
        Entity dictionaryItem = dictionariesService.getDictionaryItemDD().create();

        dictionaryItem.setField(DictionaryItemFields.NAME, addressTypeAttributes.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(DictionaryItemFields.TECHNICAL_CODE,
                addressTypeAttributes.get(L_TECHNICAL_CODE.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(DictionaryItemFields.DICTIONARY, getAddressTypeDictionary());

        dictionaryItem = dictionaryItem.getDataDefinition().save(dictionaryItem);

        if (dictionaryItem.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Address type saved {addressType : " + dictionaryItem.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved dictionaryItem entity have validation errors - "
                    + addressTypeAttributes.get(L_NAME));
        }
    }

    public void deactivateDictionaryItems(final Collection<String> technicalCodes) {
        DataDefinition dictionaryItemDD = dictionariesService.getDictionaryItemDD();

        List<Entity> dictionaryItems = dictionaryItemDD.find()
                .add(belongsTo(DictionaryItemFields.DICTIONARY, getAddressTypeDictionary()))
                .add(in(DictionaryItemFields.TECHNICAL_CODE, technicalCodes)).list().getEntities();

        dictionaryItemDD.deactivate(dictionaryItems.stream().map(Entity::getId).toArray(Long[]::new));
    }

    private boolean databaseHasToBePrepared() {
        return dictionariesService.getDictionaryItemDD().find()
                .add(belongsTo(DictionaryItemFields.DICTIONARY, getAddressTypeDictionary()))
                .add(eq(DictionaryItemFields.TECHNICAL_CODE, "01main")).list().getTotalNumberOfEntities() == 0;
    }

    private InputStream getAddressTypeXmlFile() throws IOException {
        return AddressTypeLoader.class.getResourceAsStream("/basic/model/data/addressType" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }

    public Entity getAddressTypeDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_ADDRESS_TYPE))
                .setMaxResults(1).uniqueResult();
    }

}
