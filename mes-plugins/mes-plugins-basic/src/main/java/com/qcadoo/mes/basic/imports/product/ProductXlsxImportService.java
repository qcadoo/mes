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
package com.qcadoo.mes.basic.imports.product;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductXlsxImportService extends XlsxImportService {

    private static final String L_COST_FOR_NUMBER = "costForNumber";

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity product = getDataDefinition(pluginIdentifier, modelName).create();

        product.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        product.setField(L_COST_FOR_NUMBER, BigDecimal.ONE);

        return product;
    }

}
