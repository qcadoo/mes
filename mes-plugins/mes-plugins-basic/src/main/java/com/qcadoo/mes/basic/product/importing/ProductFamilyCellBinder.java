/*
 * **************************************************************************
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;

@Component
public class ProductFamilyCellBinder implements CellBinder {

    private final DataDefinitionService dataDefinitionService;

    @Autowired
    public ProductFamilyCellBinder(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    @Override
    public void bind(Cell cell, Entity entity, BindingErrorsAccessor errorsAccessor) {
        if (null != cell) {
            String value = formatCell(cell);
            Entity familyProductCandidate = getProductDataDefinition()
                    .find()
                    .add(SearchRestrictions.eq(ProductFields.NUMBER, value))
                    .uniqueResult();

            if (null == familyProductCandidate) {
                errorsAccessor.addError("notFound");
            } else if (!PRODUCTS_FAMILY.getStringValue().equals(familyProductCandidate.getStringField(ENTITY_TYPE))) {
                errorsAccessor.addError("notFamily");
            } else {
                entity.setField(getFieldName(), familyProductCandidate);
            }
        }
    }

    @Override
    public String getFieldName() {
        return "parent";
    }
}
