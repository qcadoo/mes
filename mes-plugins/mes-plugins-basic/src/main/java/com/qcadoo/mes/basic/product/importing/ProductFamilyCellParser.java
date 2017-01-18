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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;

@Component
class ProductFamilyCellParser implements CellParser {

    private final DataDefinitionService dataDefinitionService;

    @Autowired
    ProductFamilyCellParser(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    @Override
    public void parse(String cellValue, BindingErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {
        Entity familyProductCandidate = getProductDataDefinition()
                .find()
                .add(SearchRestrictions.eq(ProductFields.NUMBER, cellValue))
                .uniqueResult();

        if (null == familyProductCandidate) {
            errorsAccessor.addError("qcadooView.validate.field.error.lookupCodeNotFound");
        } else if (!PRODUCTS_FAMILY.getStringValue().equals(familyProductCandidate.getStringField(ENTITY_TYPE))) {
            errorsAccessor.addError("basic.productsImport.error.field.notFamily");
        } else {
            valueConsumer.accept(familyProductCandidate);
        }
    }
}
