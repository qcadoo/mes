/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.masterOrders.imports.salesPlanProduct;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class SalesPlanProductXlsxImportService extends XlsxImportService {

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Override
    public void validateEntity(final Entity product, final DataDefinition productDD) {
        setAdditionalFields(product);
    }

    private void setAdditionalFields(Entity product) {
        product.setField(SalesPlanProductFields.ORDERED_QUANTITY, BigDecimal.ZERO);
        product.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN,
                product.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));
        if (product.getBelongsToField(SalesPlanProductFields.PRODUCT) != null) {
            product.setField(SalesPlanProductFields.TECHNOLOGY,
                    technologyServiceO.getDefaultTechnology(product.getBelongsToField(SalesPlanProductFields.PRODUCT)));
        }
    }

}
