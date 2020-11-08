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
package com.qcadoo.mes.basic.imports.product;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.basic.imports.parsers.DictionaryCellParsers;

@Component
public class ProductCellBinderRegistry {

    private static final String L_NOMINAL_COST = "nominalCost";

    private static final String L_LAST_PURCHASE_COST = "lastPurchaseCost";

    private static final String L_AVERAGE_COST = "averageCost";

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser globalTypeOfMaterialCellParser;

    @Autowired
    private DictionaryCellParsers dictionaryCellParsers;

    @Autowired
    private CellParser companyCellParser;

    @Autowired
    private CellParser assortmentCellParser;

    @Autowired
    private CellParser productFamilyCellParser;

    @Autowired
    private CellParser bigDecimalCellParser;

    @Autowired
    private CellParser sizeCellParser;

    @Autowired
    private CellParser integerCellParser;

    @Autowired
    private CellParser booleanCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(ProductFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(ProductFields.NAME));
        cellBinderRegistry.setCellBinder(optional(ProductFields.GLOBAL_TYPE_OF_MATERIAL, globalTypeOfMaterialCellParser));
        cellBinderRegistry.setCellBinder(required(ProductFields.UNIT, dictionaryCellParsers.units()));
        cellBinderRegistry.setCellBinder(optional(ProductFields.ADDITIONAL_UNIT, dictionaryCellParsers.units()));
        cellBinderRegistry.setCellBinder(optional(ProductFields.CONVERSION, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.EAN));
        cellBinderRegistry.setCellBinder(optional(ProductFields.CATEGORY, dictionaryCellParsers.productCategory()));
        cellBinderRegistry.setCellBinder(optional(ProductFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(ProductFields.PRODUCER, companyCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.ASSORTMENT, assortmentCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.PARENT, productFamilyCellParser));
        cellBinderRegistry.setCellBinder(optional(L_NOMINAL_COST, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(L_LAST_PURCHASE_COST, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(L_AVERAGE_COST, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.SIZE, sizeCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.EXPIRY_DATE_VALIDITY, integerCellParser));
        cellBinderRegistry.setCellBinder(optional(ProductFields.BATCH_EVIDENCE, booleanCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
