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
package com.qcadoo.mes.masterOrders.imports.masterOrder;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.imports.parsers.MasterOrderPositionStatusCellParser;
import com.qcadoo.mes.masterOrders.imports.parsers.MasterOrderStateCellParser;

@Component
public class MasterOrderCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser productCellParser;

    @Autowired
    private CellParser bigDecimalCellParser;

    @Autowired
    private CellParser companyCellParser;

    @Autowired
    private CellParser dateTimeCellParser;

    @Autowired
    private MasterOrderStateCellParser masterOrderStateCellParser;

    @Autowired
    private MasterOrderPositionStatusCellParser masterOrderPositionStatusCellParser;

    @Autowired
    private CellParser technologyCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(MasterOrderFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(MasterOrderFields.PRODUCT, productCellParser));
        cellBinderRegistry.setCellBinder(required(MasterOrderFields.MASTER_ORDER_QUANTITY, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.NAME));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.COMPANY, companyCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.START_DATE, dateTimeCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.FINISH_DATE, dateTimeCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.DEADLINE, dateTimeCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.DATE_OF_RECEIPT, dateTimeCellParser));
        cellBinderRegistry
                .setCellBinder(optional(MasterOrderFields.MASTER_ORDER_STATE, masterOrderStateCellParser.masterOrderState()));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.TECHNOLOGY, technologyCellParser));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.COMMENTS));
        cellBinderRegistry.setCellBinder(optional(MasterOrderFields.MASTER_ORDER_POSITION_STATUS,
                masterOrderPositionStatusCellParser.masterOrderPositionStatus()));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
