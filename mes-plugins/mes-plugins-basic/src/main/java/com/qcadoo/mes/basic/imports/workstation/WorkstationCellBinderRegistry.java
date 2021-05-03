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
package com.qcadoo.mes.basic.imports.workstation;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class WorkstationCellBinderRegistry {

    private static final String L_PRODUCTION_LINE = "productionLine";

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser workstationTypeCellParser;

    @Autowired
    private CellParser dateCellParser;

    @Autowired
    private CellParser divisionCellParser;

    @Autowired
    private CellParser productionLineCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(WorkstationFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(WorkstationFields.NAME));
        cellBinderRegistry.setCellBinder(required(WorkstationFields.WORKSTATION_TYPE, workstationTypeCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.SERIAL_NUMBER));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.UDT_NUMBER));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.SERIES));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.PRODUCER));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.PRODUCTION_DATE, dateCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.DATE_OF_ADMISSION, dateCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.DATE_OF_WITHDRAWAL, dateCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.WNK_NUMBER));
        cellBinderRegistry.setCellBinder(optional(WorkstationFields.DIVISION, divisionCellParser));
        cellBinderRegistry.setCellBinder(optional(L_PRODUCTION_LINE, productionLineCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
