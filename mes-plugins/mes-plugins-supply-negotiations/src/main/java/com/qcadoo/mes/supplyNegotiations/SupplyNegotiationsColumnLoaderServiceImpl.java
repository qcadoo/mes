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
package com.qcadoo.mes.supplyNegotiations;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.OperationType;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class SupplyNegotiationsColumnLoaderServiceImpl implements SupplyNegotiationsColumnLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(SupplyNegotiationsColumnLoaderServiceImpl.class);

    private static final String L_COLUMN_FOR_REQUESTS = "columnForRequests";

    private static final String L_COLUMN_FOR_OFFERS = "columnForOffers";

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void fillColumnsForRequests(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_REQUESTS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_REQUESTS, OperationType.ADD, columnAttributes);
        }
    }

    @Override
    public void clearColumnsForRequests(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_REQUESTS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_REQUESTS, OperationType.DELETE, columnAttributes);
        }
    }

    @Override
    public void fillColumnsForOffers(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_OFFERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_OFFERS, OperationType.ADD, columnAttributes);
        }
    }

    @Override
    public void clearColumnsForOffers(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_OFFERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_OFFERS, OperationType.DELETE, columnAttributes);
        }
    }

    private void readData(final String type, final OperationType operation, final Map<String, String> values) {
        if (L_COLUMN_FOR_REQUESTS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForRequests(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForRequests(values);
            }
        } else if (L_COLUMN_FOR_OFFERS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForOffers(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForOffers(values);
            }
        }
    }

    private void addColumnForRequests(final Map<String, String> columnAttributes) {
        Entity column = columnExtensionService.addColumn(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS, columnAttributes);
        Entity parameter = parameterService.getParameter();
        column.setField("parameter", parameter);
        column.getDataDefinition().save(column);

        addParameterColumnForRequests(column);

    }

    private void addParameterColumnForRequests(final Entity columnForRequest) {
        Entity parameterRequestColumn = dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_PARAMETER_COLUMN_FOR_REQUESTS).create();
        parameterRequestColumn.setField(BasicConstants.MODEL_PARAMETER, parameterService.getParameter());
        parameterRequestColumn.setField(SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS, columnForRequest);
        parameterRequestColumn = parameterRequestColumn.getDataDefinition().save(parameterRequestColumn);

        if (parameterRequestColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter orders columns {column=" + parameterRequestColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrdersColumn - has validation errors - "
                    + parameterRequestColumn.toString());
        }
    }

    private void deleteColumnForRequests(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS, columnAttributes);
    }

    private void addColumnForOffers(final Map<String, String> columnAttributes) {
        Entity column = columnExtensionService.addColumn(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS, columnAttributes);
        Entity parameter = parameterService.getParameter();
        column.setField("parameter", parameter);
        column.getDataDefinition().save(column);

        addParameterColumnForOffers(column);
    }

    private void addParameterColumnForOffers(final Entity columnForOffer) {
        Entity parameterOfferColumn = dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_PARAMETER_COLUMN_FOR_OFFERS).create();
        parameterOfferColumn.setField(BasicConstants.MODEL_PARAMETER, parameterService.getParameter());
        parameterOfferColumn.setField(SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS, columnForOffer);
        parameterOfferColumn = parameterOfferColumn.getDataDefinition().save(parameterOfferColumn);

        if (parameterOfferColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter orders columns {column=" + parameterOfferColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrdersColumn - has validation errors - "
                    + parameterOfferColumn.toString());
        }
    }

    private void deleteColumnForOffers(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS, columnAttributes);
    }

    public boolean isColumnsForRequestsEmpty() {
        return columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS);
    }

    public boolean isColumnsForOffersEmpty() {
        return columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS);
    }

}
