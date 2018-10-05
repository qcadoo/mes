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
package com.qcadoo.mes.catNumbersInNegot.columnExtension;

import static com.qcadoo.mes.catNumbersInNegot.contants.RequestForQuotationProductFieldsCNIN.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.supplyNegotiations.print.RequestForQuotationColumnFiller;
import com.qcadoo.model.api.Entity;

@Component
public class SupplyNegotiationsColumnFillerCNIN implements RequestForQuotationColumnFiller {

    @Override
    public Map<Entity, Map<String, String>> getRequestForQuotationProductsColumnValues(
            final List<Entity> requestForQuotationProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity requestForQuotationProduct : requestForQuotationProducts) {
            if (!values.containsKey(requestForQuotationProduct)) {
                values.put(requestForQuotationProduct, new HashMap<String, String>());
            }

            fillCatalogNumber(values, requestForQuotationProduct);
        }

        return values;
    }

    private void fillCatalogNumber(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationProduct) {
        String catalogNumber = null;

        if (requestForQuotationProduct == null) {
            catalogNumber = "";
        } else {
            Entity productCatalogNumber = requestForQuotationProduct.getBelongsToField(PRODUCT_CATALOG_NUMBER);

            if (productCatalogNumber == null) {
                catalogNumber = "";
            } else {
                catalogNumber = productCatalogNumber.getStringField(CATALOG_NUMBER);
            }
        }

        values.get(requestForQuotationProduct).put("catalogNumber", catalogNumber);
    }

}
