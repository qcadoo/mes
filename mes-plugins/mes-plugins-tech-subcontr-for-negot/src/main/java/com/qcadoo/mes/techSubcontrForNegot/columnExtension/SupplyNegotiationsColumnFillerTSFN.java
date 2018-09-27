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
package com.qcadoo.mes.techSubcontrForNegot.columnExtension;

import static com.qcadoo.mes.techSubcontrForNegot.constants.RequestForQuotationProductFieldsTSFN.OPERATION;
import static com.qcadoo.mes.technologies.constants.OperationFields.NUMBER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.supplyNegotiations.print.OfferColumnFiller;
import com.qcadoo.mes.supplyNegotiations.print.RequestForQuotationColumnFiller;
import com.qcadoo.model.api.Entity;

@Component
public class SupplyNegotiationsColumnFillerTSFN implements RequestForQuotationColumnFiller, OfferColumnFiller {

    @Override
    public Map<Entity, Map<String, String>> getRequestForQuotationProductsColumnValues(
            final List<Entity> requestForQuotationProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity requestForQuotationProduct : requestForQuotationProducts) {
            if (!values.containsKey(requestForQuotationProduct)) {
                values.put(requestForQuotationProduct, new HashMap<String, String>());
            }

            fillOperationNumber(values, requestForQuotationProduct);
        }

        return values;
    }

    @Override
    public Map<Entity, Map<String, String>> getOfferProductsColumnValues(final List<Entity> offerProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity offerProduct : offerProducts) {
            if (!values.containsKey(offerProduct)) {
                values.put(offerProduct, new HashMap<String, String>());
            }

            fillOperationNumber(values, offerProduct);
        }

        return values;
    }

    private void fillOperationNumber(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationOrOfferProduct) {
        String operationNumber = null;

        if (requestForQuotationOrOfferProduct == null) {
            operationNumber = "";
        } else {
            Entity operation = requestForQuotationOrOfferProduct.getBelongsToField(OPERATION);

            if (operation == null) {
                operationNumber = "";
            } else {
                operationNumber = operation.getStringField(NUMBER);
            }
        }

        values.get(requestForQuotationOrOfferProduct).put("operationNumber", operationNumber);
    }

}
