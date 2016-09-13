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
public class TypeOfPalletLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TypeOfPalletLoader.class);

    private static final String L_NAME = "name";

    private static final String L_TECHNICAL_CODE = "technicalCode";

    private static final String L_TYPE_OF_PALLET = "typeOfPallet";

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DictionariesService dictionariesService;

    public final void loadTypeOfPallets() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Type of pallets will be populated ...");
            }

            Map<Integer, Map<String, String>> typeOfPalletsAttributes = getTypeOfPalletsAttributesFromXML();

            typeOfPalletsAttributes.values().stream()
                    .forEach(typeOfPalletAttributes -> addDictionaryItem(typeOfPalletAttributes));
        }
    }

    public void unloadTypeOfPallets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Type of pallets will be unpopulated ...");
        }

        Map<Integer, Map<String, String>> typeOfPalletsAttributes = getTypeOfPalletsAttributesFromXML();

        List<String> technicalCodes = typeOfPalletsAttributes.values().stream()
                .map(typeOfPalletAttributes -> typeOfPalletAttributes.get(L_TECHNICAL_CODE)).filter(Objects::nonNull)
                .collect(Collectors.toList());

        deactivateDictionaryItems(technicalCodes);
    }

    private Map<Integer, Map<String, String>> getTypeOfPalletsAttributesFromXML() {
        LOG.info("Loading data from typeOfPallet" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml ...");

        Map<Integer, Map<String, String>> typeOfPalletsAttributes = Maps.newHashMap();

        try {
            SAXBuilder builder = new SAXBuilder();
            
            Document document = builder.build(getTypeOfPalletXmlFile());
            Element rootNode = document.getRootElement();
            
            @SuppressWarnings("unchecked")
            List<Element> listOfRows = rootNode.getChildren("row");

            for (int rowNum = 0; rowNum < listOfRows.size(); rowNum++) {
                parseAndAddTypeOfPallet(typeOfPalletsAttributes, listOfRows, rowNum);
            }
        } catch (IOException | JDOMException e) {
            LOG.error(e.getMessage(), e);
        }

        return typeOfPalletsAttributes;
    }

    private void parseAndAddTypeOfPallet(final Map<Integer, Map<String, String>> typeOfPalletsAttributes, final List<Element> listOfRows, final int rowNum) {
        Element node = listOfRows.get(rowNum);

        @SuppressWarnings("unchecked")
        List<Attribute> listOfAtributes = node.getAttributes();

        Map<String, String> typeOfPalletAttributes = Maps.newHashMap();

        for (int attributeNum = 0; attributeNum < listOfAtributes.size(); attributeNum++) {
            typeOfPalletAttributes.put(listOfAtributes.get(attributeNum).getName().toLowerCase(Locale.ENGLISH),
                    listOfAtributes.get(attributeNum).getValue());
        }

        typeOfPalletsAttributes.put(rowNum, typeOfPalletAttributes);
    }

    private void addDictionaryItem(final Map<String, String> typeOfPalletAttributes) {
        Entity dictionaryItem = dictionariesService.getDictionaryItemDD().create();

        dictionaryItem.setField(DictionaryItemFields.NAME, typeOfPalletAttributes.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(DictionaryItemFields.TECHNICAL_CODE,
                typeOfPalletAttributes.get(L_TECHNICAL_CODE.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(DictionaryItemFields.DICTIONARY, getTypeOfPalletDictionary());

        dictionaryItem = dictionaryItem.getDataDefinition().save(dictionaryItem);

        if (dictionaryItem.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Type of pallet saved {typeOfPallet : " + dictionaryItem.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved dictionaryItem entity have validation errors - "
                    + typeOfPalletAttributes.get(L_NAME));
        }
    }

    public void deactivateDictionaryItems(final Collection<String> technicalCodes) {
        DataDefinition dictionaryItemDD = dictionariesService.getDictionaryItemDD();

        List<Entity> dictionaryItems = dictionaryItemDD.find()
                .add(belongsTo(DictionaryItemFields.DICTIONARY, getTypeOfPalletDictionary()))
                .add(in(DictionaryItemFields.TECHNICAL_CODE, technicalCodes)).list().getEntities();

        dictionaryItemDD.deactivate(dictionaryItems.stream().map(Entity::getId).toArray(Long[]::new));
    }

    private boolean databaseHasToBePrepared() {
        return dictionariesService
                .getDictionaryItemDD()
                .find()
                .add(belongsTo(DictionaryItemFields.DICTIONARY, getTypeOfPalletDictionary()))
                .add(SearchRestrictions.or(eq(DictionaryItemFields.TECHNICAL_CODE, "01epal"),
                        eq(DictionaryItemFields.TECHNICAL_CODE, "02cheapEur"))).list().getTotalNumberOfEntities() == 0;
    }

    private InputStream getTypeOfPalletXmlFile() throws IOException {
        return TypeOfPalletLoader.class.getResourceAsStream("/basic/model/data/typeOfPallet" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }

    public Entity getTypeOfPalletDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_TYPE_OF_PALLET))
                .setMaxResults(1).uniqueResult();
    }

}
