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

@Component
class RowProcessorFactory {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CellBinder globalTypeOfMaterialCellBinder;

    @Autowired
    private CellBinder producerCellBinder;

    @Autowired
    private CellBinder assortmentCellBinder;

    @Autowired
    private CellBinder productFamilyCellBinder;

    private CellBinder[] cellBinders;

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    @PostConstruct
    private void init() {
        cellBinders = new CellBinder[]{
                new SimpleCellBinder(ProductFields.NUMBER, true),
                new SimpleCellBinder(ProductFields.NAME, true),
                globalTypeOfMaterialCellBinder,
                new SimpleCellBinder(ProductFields.UNIT, true),
                new SimpleCellBinder(ProductFields.EAN, false),
                new SimpleCellBinder(ProductFields.CATEGORY, false), // TODO check if simple binder is enough to validate if category is active
                new SimpleCellBinder(ProductFields.DESCRIPTION, false),
                producerCellBinder,
                assortmentCellBinder,
                productFamilyCellBinder,
                new SimpleCellBinder("nominalCost", false),
                new SimpleCellBinder("lastOfferCost", false),
                new SimpleCellBinder("averageOfferCost", false),
        };
    }

    RowProcessor create(final ImportStatus importStatus, int rowIndx) {
        DataDefinition dataDefinition = getProductDataDefinition();
        Entity entity = dataDefinition.create();
        entity.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        return new RowProcessor(importStatus, dataDefinition, entity, rowIndx, cellBinders);
    }
}
