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
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.product.importing.CellBinder.optional;
import static com.qcadoo.mes.basic.product.importing.CellBinder.required;

@Component
class RowProcessorFactory {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CellParser globalTypeOfMaterialCellParser;

    @Autowired
    private CellParser producerCellParser;

    @Autowired
    private CellParser assortmentCellParser;

    @Autowired
    private CellParser productFamilyCellParser;

    private CellBinder[] cellBinders;

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    @PostConstruct
    private void init() {
        BigDecimalCellParser bigDecimalCellParser = new BigDecimalCellParser();
        cellBinders = new CellBinder[]{
                required(ProductFields.NUMBER),
                required(ProductFields.NAME),
                optional(ProductFields.GLOBAL_TYPE_OF_MATERIAL, globalTypeOfMaterialCellParser),
                required(ProductFields.UNIT),
                optional(ProductFields.EAN),
                // TODO check if simple binder is enough to validate if category is active
                optional(ProductFields.CATEGORY),
                optional(ProductFields.DESCRIPTION),
                optional(ProductFields.PRODUCER, producerCellParser),
                optional(ProductFields.ASSORTMENT, assortmentCellParser),
                optional(ProductFields.PARENT, productFamilyCellParser),

                // TODO That's the reason why we should move import functionality to CNFP plugin
                // More sophisticated approach is to make import functionality expandable by other plugins
                // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.NOMINAL_COST
                optional("nominalCost", bigDecimalCellParser),
                // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.LAST_OFFER_COST
                optional("lastOfferCost", bigDecimalCellParser),
                // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.AVERAGE_OFFER_COST
                optional("averageOfferCost", bigDecimalCellParser)
        };
    }

    RowProcessor create(final ImportStatus importStatus, int rowIndx) {
        DataDefinition dataDefinition = getProductDataDefinition();
        Entity entity = dataDefinition.create();
        entity.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        return new RowProcessor(importStatus, dataDefinition, entity, rowIndx, cellBinders);
    }
}
