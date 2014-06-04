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
package com.qcadoo.mes.columnExtension;

import static com.qcadoo.mes.columnExtension.constants.ColumnFields.ALIGNMENT;
import static com.qcadoo.mes.columnExtension.constants.ColumnFields.COLUMN_FILLER;
import static com.qcadoo.mes.columnExtension.constants.ColumnFields.DESCRIPTION;
import static com.qcadoo.mes.columnExtension.constants.ColumnFields.IDENTIFIER;
import static com.qcadoo.mes.columnExtension.constants.ColumnFields.NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ColumnExtensionServiceImpl implements ColumnExtensionService {

    private static final Logger LOG = LoggerFactory.getLogger(ColumnExtensionServiceImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Map<Integer, Map<String, String>> getColumnsAttributesFromXML(final String plugin, final String file) {
        LOG.info("Loading data from " + file + ".xml ...");

        Map<Integer, Map<String, String>> columnsAttributes = Maps.newHashMap();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(plugin, file));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> listOfRows = rootNode.getChildren("row");

            for (int rowNum = 0; rowNum < listOfRows.size(); rowNum++) {
                Element node = listOfRows.get(rowNum);

                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtributes = node.getAttributes();

                Map<String, String> columnAttribute = Maps.newHashMap();

                for (int attributeNum = 0; attributeNum < listOfAtributes.size(); attributeNum++) {
                    columnAttribute.put(listOfAtributes.get(attributeNum).getName().toLowerCase(Locale.ENGLISH), listOfAtributes
                            .get(attributeNum).getValue());
                }

                columnsAttributes.put(rowNum, columnAttribute);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }

        return columnsAttributes;
    }

    private InputStream getXmlFile(final String plugin, final String file) throws IOException {
        return ColumnExtensionServiceImpl.class.getResourceAsStream("/" + plugin + "/model/data/" + file + ".xml");
    }

    public Entity addColumn(final String pluginIdentifier, final String model, final Map<String, String> columnAttributes) {
        Entity column = getColumnDD(pluginIdentifier, model).create();

        column.setField(IDENTIFIER, columnAttributes.get(IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        column.setField(NAME, columnAttributes.get(NAME.toLowerCase(Locale.ENGLISH)));
        column.setField(DESCRIPTION, columnAttributes.get(DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        column.setField(COLUMN_FILLER, columnAttributes.get(COLUMN_FILLER.toLowerCase(Locale.ENGLISH)));
        column.setField(ALIGNMENT, columnAttributes.get(ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for " + model + " item {column=" + column.getStringField(NAME) + "}");
        }

        column = column.getDataDefinition().save(column);

        if (column.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for " + model + " saved {column=" + column.toString() + "}");
            }

            return column;
        } else {
            throw new IllegalStateException("Saved entity " + model + " has validation errors - "
                    + columnAttributes.get(IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        }
    }

    public void deleteColumn(final String pluginIdentifier, final String model, final Map<String, String> columnAttributes) {
        final List<Entity> columns = getColumnDD(pluginIdentifier, model).find()
                .add(SearchRestrictions.eq(IDENTIFIER, columnAttributes.get(IDENTIFIER))).list().getEntities();

        for (Entity column : columns) {
            column.getDataDefinition().delete(column.getId());
        }
    }

    public boolean isColumnsEmpty(final String pluginIdentifier, final String model) {
        return getColumnDD(pluginIdentifier, model).find().list().getTotalNumberOfEntities() == 0;
    }

    private DataDefinition getColumnDD(final String pluginIdentifier, final String model) {
        return dataDefinitionService.get(pluginIdentifier, model);
    }

    @Override
    public List<Entity> filterEmptyColumns(final List<Entity> columns, final List<Entity> rows,
            final Map<Entity, Map<String, String>> columnValues) {
        List<Entity> filteredColumns = Lists.newArrayList();

        for (Entity column : columns) {
            String identifier = column.getStringField(IDENTIFIER);

            boolean isEmpty = true;

            for (Entity row : rows) {
                String value = columnValues.get(row).get(identifier);

                if (StringUtils.isNotEmpty(value)) {
                    isEmpty = false;

                    break;
                }
            }

            if (!isEmpty) {
                filteredColumns.add(column);
            }
        }

        return filteredColumns;
    }

}
