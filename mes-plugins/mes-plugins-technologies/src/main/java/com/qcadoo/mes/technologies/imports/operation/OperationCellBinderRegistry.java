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
package com.qcadoo.mes.technologies.imports.operation;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.technologies.constants.OperationFields;

@Component
public class OperationCellBinderRegistry {

    private static final String L_TPZ = "tpz";

    private static final String L_TJ = "tj";

    private static final String L_PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String L_TIME_NEXT_OPERATION = "timeNextOperation";

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser productCellParser;

    @Autowired
    private CellParser divisionCellParser;

    @Autowired
    private CellParser workstationCellParser;

    @Autowired
    private CellParser integerCellParser;

    @Autowired
    private CellParser bigDecimalCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(OperationFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(OperationFields.NAME));
        cellBinderRegistry.setCellBinder(optional(OperationFields.PRODUCT, productCellParser));
        cellBinderRegistry.setCellBinder(optional(OperationFields.COMMENT));
        cellBinderRegistry.setCellBinder(optional(OperationFields.DIVISION, divisionCellParser));
        cellBinderRegistry.setCellBinder(optional(OperationFields.WORKSTATION, workstationCellParser));
        cellBinderRegistry.setCellBinder(optional(L_TPZ, integerCellParser));
        cellBinderRegistry.setCellBinder(optional(L_TJ, integerCellParser));
        cellBinderRegistry.setCellBinder(optional(L_PRODUCTION_IN_ONE_CYCLE, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(L_TIME_NEXT_OPERATION, integerCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
