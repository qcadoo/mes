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
package com.qcadoo.mes.catNumbersInNegot.hooks;

import static com.qcadoo.mes.catNumbersInNegot.contants.RequestForQuotationProductFieldsCNIN.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.PRODUCT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class RequestForQuotationHooksCNIN {

    @Autowired
    private ProductCatalogNumbersService productCatalogNumbersService;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public void updateRequestForQuotationProductsCatalogNumbers(final DataDefinition requestForQuotationDD,
            final Entity requestForQuotation) {
        Entity supplier = requestForQuotation.getBelongsToField(SUPPLIER);

        if ((requestForQuotation.getId() != null) && hasSupplierChanged(requestForQuotation.getId(), supplier)) {
            List<Entity> requestForQuotationProducts = requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS);

            if (requestForQuotationProducts != null) {
                for (Entity requestForQuotationProduct : requestForQuotationProducts) {
                    Entity product = requestForQuotationProduct.getBelongsToField(PRODUCT);

                    Entity productCatalogNumber = productCatalogNumbersService.getProductCatalogNumber(product, supplier);

                    if (productCatalogNumber != null) {
                        requestForQuotationProduct.setField(PRODUCT_CATALOG_NUMBER, productCatalogNumber);

                        requestForQuotationProduct.getDataDefinition().save(requestForQuotationProduct);
                    }
                }
            }
        }
    }

    private boolean hasSupplierChanged(final Long requestForQuotationId, final Entity supplier) {
        Entity existingRequestForQuotation = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        Entity existingSupplier = existingRequestForQuotation.getBelongsToField(SUPPLIER);

        return !existingSupplier.equals(supplier);
    }

}
