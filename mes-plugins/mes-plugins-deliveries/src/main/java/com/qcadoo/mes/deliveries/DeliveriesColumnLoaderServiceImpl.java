/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.deliveries;

import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.ALIGNMENT;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.COLUMN_FILLER;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.IDENTIFIER;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.NAME;

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
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveriesColumnLoaderServiceImpl implements DeliveriesColumnLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveriesColumnLoaderServiceImpl.class);

    @Autowired
    private DeliveriesService deliveriesService;

    private static final String L_COLUMN_FOR_DELIVERIES = "columnForDeliveries";

    private static final String L_COLUMN_FOR_ORDERS = "columnForOrders";

    private static enum OperationType {
        ADD, DELETE;
    };

    @Override
    public void fillColumnsForDeliveries(String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_DELIVERIES, OperationType.ADD);
    }

    @Override
    public void clearColumnsForDeliveries(String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_DELIVERIES, OperationType.DELETE);
    }

    @Override
    public void fillColumnsForOrders(String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_ORDERS, OperationType.ADD);
    }

    @Override
    public void clearColumnsForOrders(String plugin) {
        readDataFromXML(plugin, L_COLUMN_FOR_ORDERS, OperationType.DELETE);
    }

    private void readDataFromXML(final String plugin, final String type, final OperationType operation) {
        LOG.info("Loading data from " + type + ".xml ...");

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(getXmlFile(plugin, type));
            Element rootNode = document.getRootElement();
            @SuppressWarnings("unchecked")
            List<Element> list = rootNode.getChildren("row");

            for (int i = 0; i < list.size(); i++) {
                Element node = list.get(i);
                @SuppressWarnings("unchecked")
                List<Attribute> listOfAtributes = node.getAttributes();
                Map<String, String> values = new HashMap<String, String>();

                for (int j = 0; j < listOfAtributes.size(); j++) {
                    values.put(listOfAtributes.get(j).getName().toLowerCase(Locale.ENGLISH), listOfAtributes.get(j).getValue());
                }
                readData(type, operation, values);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (JDOMException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readData(final String type, final OperationType operation, final Map<String, String> values) {
        if (L_COLUMN_FOR_DELIVERIES.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForDeliveries(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForDeliveries(values);
            }
        } else if (L_COLUMN_FOR_ORDERS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForOrders(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForOrders(values);
            }
        }
    }

    private void addColumnForDeliveries(final Map<String, String> values) {
        Entity columnForDeliveries = deliveriesService.getColumnForDeliveriesDD().create();

        columnForDeliveries.setField(IDENTIFIER, values.get(IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        columnForDeliveries.setField(NAME, values.get(NAME.toLowerCase(Locale.ENGLISH)));
        columnForDeliveries.setField(DESCRIPTION, values.get(DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        columnForDeliveries.setField(COLUMN_FILLER, values.get(COLUMN_FILLER.toLowerCase(Locale.ENGLISH)));
        columnForDeliveries.setField(ALIGNMENT, values.get(ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for deliveries item {column=" + columnForDeliveries.getStringField(NAME) + "}");
        }

        columnForDeliveries = columnForDeliveries.getDataDefinition().save(columnForDeliveries);

        if (columnForDeliveries.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for deliveries saved {column=" + columnForDeliveries.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForDeliveries - has validation errors - "
                    + values.get(NAME.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForDeliveries(final Map<String, String> values) {
        final List<Entity> columnsForDeliveries = deliveriesService.getColumnForDeliveriesDD().find()
                .add(SearchRestrictions.eq(IDENTIFIER, values.get(IDENTIFIER))).list().getEntities();

        for (Entity columnForDeliveries : columnsForDeliveries) {
            deliveriesService.getColumnForDeliveriesDD().delete(columnForDeliveries.getId());
        }
    }

    private void addColumnForOrders(final Map<String, String> values) {
        Entity columnForOrders = deliveriesService.getColumnForOrdersDD().create();

        columnForOrders.setField(IDENTIFIER, values.get(IDENTIFIER.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(NAME, values.get(NAME.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(DESCRIPTION, values.get(DESCRIPTION.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(COLUMN_FILLER, values.get(COLUMN_FILLER.toLowerCase(Locale.ENGLISH)));
        columnForOrders.setField(ALIGNMENT, values.get(ALIGNMENT.toLowerCase(Locale.ENGLISH)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Add column for orders item {column=" + columnForOrders.getStringField(NAME) + "}");
        }

        columnForOrders = columnForOrders.getDataDefinition().save(columnForOrders);

        if (columnForOrders.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column for orders saved {column=" + columnForOrders.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - columnForOrders - has validation errors - "
                    + values.get(NAME.toUpperCase(Locale.ENGLISH)));
        }
    }

    private void deleteColumnForOrders(final Map<String, String> values) {
        final List<Entity> columnsForOrders = deliveriesService.getColumnForOrdersDD().find()
                .add(SearchRestrictions.eq(IDENTIFIER, values.get(IDENTIFIER))).list().getEntities();

        for (Entity columnForOrder : columnsForOrders) {
            deliveriesService.getColumnForOrdersDD().delete(columnForOrder.getId());
        }
    }

    public boolean isColumnsForDeliveriesEmpty() {
        return deliveriesService.getColumnForDeliveriesDD().find().list().getTotalNumberOfEntities() == 0;
    }

    public boolean isColumnsForOrdersEmpty() {
        return deliveriesService.getColumnForOrdersDD().find().list().getTotalNumberOfEntities() == 0;
    }

    private InputStream getXmlFile(final String plugin, final String type) throws IOException {
        return DeliveriesColumnLoaderServiceImpl.class.getResourceAsStream("/" + plugin + "/model/data/" + type + ".xml");
    }

}
