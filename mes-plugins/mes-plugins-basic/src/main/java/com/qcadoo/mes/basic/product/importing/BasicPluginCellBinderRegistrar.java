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

import com.qcadoo.mes.basic.constants.ProductFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.product.importing.CellBinder.optional;
import static com.qcadoo.mes.basic.product.importing.CellBinder.required;

@Component
class BasicPluginCellBinderRegistrar {

    @Autowired
    private CellBinderRegistry cellBinderRegistry;

    @Autowired
    private CellParser globalTypeOfMaterialCellParser;

    @Autowired
    private CellParser producerCellParser;

    @Autowired
    private CellParser assortmentCellParser;

    @Autowired
    private CellParser productFamilyCellParser;

    @Autowired
    private DictionaryCellParsers dictionaryCellParsers;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(ProductFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(ProductFields.NAME));
        cellBinderRegistry.setCellBinder(optional(ProductFields.GLOBAL_TYPE_OF_MATERIAL, globalTypeOfMaterialCellParser));
        cellBinderRegistry.setCellBinder(required(ProductFields.UNIT, dictionaryCellParsers.units()));
        cellBinderRegistry.setCellBinder(optional(ProductFields.EAN));
        cellBinderRegistry.setCellBinder(optional(ProductFields.CATEGORY, dictionaryCellParsers.productCategory()));
        cellBinderRegistry.setCellBinder(optional(ProductFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(ProductFields.PRODUCER, producerCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.ASSORTMENT, assortmentCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.PARENT, productFamilyCellParser));

        BigDecimalCellParser bigDecimalCellParser = new BigDecimalCellParser();
        // TODO That's the reason why we should move import functionality to CNFP plugin
        // More sophisticated approach is to make import functionality expandable by other plugins
        // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.NOMINAL_COST
        cellBinderRegistry.setCellBinder(optional("nominalCost", bigDecimalCellParser));
        // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.LAST_PURCHASE_COST
        cellBinderRegistry.setCellBinder(optional("lastPurchaseCost", bigDecimalCellParser));
        // com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP.AVERAGE_COST
        cellBinderRegistry.setCellBinder(optional("averageCost", bigDecimalCellParser));
    }

}
