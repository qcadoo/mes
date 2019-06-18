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
package com.qcadoo.mes.negotForOrderSupplies.columnExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.negotForOrderSupplies.constants.CoverageProductFieldsNFOS;
import com.qcadoo.mes.orderSupplies.print.MaterialRequirementCoverageColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component
public class OrderSuppliesColumnFillerNFOS implements MaterialRequirementCoverageColumnFiller {

    @Autowired
    private NumberService numberService;

    @Override
    public Map<Entity, Map<String, String>> getCoverageProductsColumnValues(final List<Entity> coverageProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity coverageProduct : coverageProducts) {
            if (!values.containsKey(coverageProduct)) {
                values.put(coverageProduct, new HashMap<String, String>());
            }

            fillNegotiatedQuantity(values, coverageProduct);
        }

        return values;
    }

    private void fillNegotiatedQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal negotiatedQuantity = null;

        if (coverageProduct == null) {
            negotiatedQuantity = BigDecimal.ZERO;
        } else {
            negotiatedQuantity = coverageProduct.getDecimalField(CoverageProductFieldsNFOS.NEGOTIATED_QUANTITY);

            if (negotiatedQuantity == null) {
                negotiatedQuantity = BigDecimal.ZERO;
            }
        }

        values.get(coverageProduct).put("negotiatedQuantity", numberService.format(negotiatedQuantity));
    }

}
