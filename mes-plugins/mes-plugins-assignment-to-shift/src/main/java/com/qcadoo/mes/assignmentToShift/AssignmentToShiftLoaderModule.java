/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.assignmentToShift;

import java.io.IOException;
import java.io.InputStream;
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

import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.tenant.api.DefaultLocaleResolver;

@Component
public class AssignmentToShiftLoaderModule extends Module {

    private static final String L_QCADOO_MODEL = "qcadooModel";

    protected static final Logger LOG = LoggerFactory.getLogger(AssignmentToShiftLoaderModule.class);

    private static final String L_OCCUPATION_TYPE = "occupationType";

    private static final String L_DICTIONARY_ITEM = "dictionaryItem";

    private static final String L_DICTIONARY = "dictionary";

    private static final String L_TECHNICAL_CODE = "technicalCode";

    private static final String L_NAME = "name";

    @Autowired
    private DefaultLocaleResolver defaultLocaleResolver;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public final void multiTenantEnable() {
        if (databaseHasToBePrepared()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Occupation type table will be populated ...");
            }

            Map<Integer, Map<String, String>> occupationTypesAttributes = getOccupationTypesAttributesFromXML();

            for (Map<String, String> occupationTypeAttributes : occupationTypesAttributes.values()) {
                addDictionaryItem(occupationTypeAttributes);
            }
        }
    }

    @Override
    @Transactional
    public void multiTenantDisable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Occupation type table will be unpopulated ...");
        }

        Map<Integer, Map<String, String>> occupationTypesAttributes = getOccupationTypesAttributesFromXML();

        for (Map<String, String> occupationTypeAttributes : occupationTypesAttributes.values()) {
            deleteDictionaryItem(occupationTypeAttributes);
        }
    }

    private Map<Integer, Map<String, String>> getOccupationTypesAttributesFromXML() {
        LOG.info("Loading test data from occupationType" + "_" + defaultLocaleResolver.getDefaultLocale().getLanguage()
                + ".xml ...");

        Map<Integer, Map<String, String>> occupationTypesAttributes = Maps.newHashMap();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getOccupationTypeXmlFile());
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> listOfRows = rootNode.getChildren("row");

            for (int rowNum = 0; rowNum < listOfRows.size(); rowNum++) {
                Element node = listOfRows.get(rowNum);

                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtributes = node.getAttributes();

                Map<String, String> occupationTypeAttributes = Maps.newHashMap();

                for (int attributeNum = 0; attributeNum < listOfAtributes.size(); attributeNum++) {
                    occupationTypeAttributes.put(listOfAtributes.get(attributeNum).getName().toLowerCase(Locale.ENGLISH),
                            listOfAtributes.get(attributeNum).getValue());
                }

                occupationTypesAttributes.put(rowNum, occupationTypeAttributes);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }

        return occupationTypesAttributes;
    }

    private void addDictionaryItem(final Map<String, String> occupationTypeAttributes) {
        Entity dictionaryItem = getDictionaryItemDataDefinition().create();

        dictionaryItem.setField(L_TECHNICAL_CODE, occupationTypeAttributes.get(L_TECHNICAL_CODE.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(L_NAME, occupationTypeAttributes.get(L_NAME.toLowerCase(Locale.ENGLISH)));
        dictionaryItem.setField(L_DICTIONARY, getOcupationTypeDictionary());

        dictionaryItem = dictionaryItem.getDataDefinition().save(dictionaryItem);

        if (dictionaryItem.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Occupation type saved {occupationType : " + dictionaryItem.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved dictionaryItem entity have validation errors - "
                    + occupationTypeAttributes.get(L_NAME));
        }
    }

    public void deleteDictionaryItem(final Map<String, String> occupationTypeAttributes) {
        List<Entity> dictionaryItems = getDictionaryItemDataDefinition()
                .find()
                .add(SearchRestrictions.belongsTo(L_DICTIONARY, getOcupationTypeDictionary()))
                .add(SearchRestrictions.or(
                        SearchRestrictions.eq(L_TECHNICAL_CODE,
                                occupationTypeAttributes.get(L_TECHNICAL_CODE.toLowerCase(Locale.ENGLISH))),
                        SearchRestrictions.eq(L_NAME, occupationTypeAttributes.get(L_NAME.toLowerCase(Locale.ENGLISH))))).list()
                .getEntities();

        for (Entity dictionaryItem : dictionaryItems) {
            dictionaryItem.getDataDefinition().delete(dictionaryItem.getId());
        }
    }

    private boolean databaseHasToBePrepared() {
        return getDictionaryItemDataDefinition()
                .find()
                .add(SearchRestrictions.belongsTo(L_DICTIONARY, getOcupationTypeDictionary()))
                .add(SearchRestrictions.or(SearchRestrictions.eq(L_TECHNICAL_CODE, "01workForLine"),
                        SearchRestrictions.eq(L_TECHNICAL_CODE, "02otherCase"))).list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getDictionaryItemDataDefinition() {
        return dataDefinitionService.get(L_QCADOO_MODEL, L_DICTIONARY_ITEM);
    }

    private Entity getOcupationTypeDictionary() {
        return dataDefinitionService.get(L_QCADOO_MODEL, L_DICTIONARY).find()
                .add(SearchRestrictions.eq(L_NAME, L_OCCUPATION_TYPE)).setMaxResults(1).uniqueResult();
    }

    private InputStream getOccupationTypeXmlFile() throws IOException {
        return AssignmentToShiftLoaderModule.class.getResourceAsStream("/assignmentToShift/model/data/occupationType" + "_"
                + defaultLocaleResolver.getDefaultLocale().getLanguage() + ".xml");
    }

}
