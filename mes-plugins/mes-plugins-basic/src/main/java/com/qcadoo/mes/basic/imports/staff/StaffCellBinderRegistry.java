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
package com.qcadoo.mes.basic.imports.staff;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

@Component
public class StaffCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser companyCellParser;

    @Autowired
    private CellParser divisionCellParser;

    @Autowired
    private CellParser workstationCellParser;

    @Autowired
    private CellParser crewCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(StaffFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(StaffFields.NAME));
        cellBinderRegistry.setCellBinder(required(StaffFields.SURNAME));
        cellBinderRegistry.setCellBinder(optional(StaffFields.EMAIL));
        cellBinderRegistry.setCellBinder(optional(StaffFields.PHONE));
        cellBinderRegistry.setCellBinder(optional(StaffFields.WORK_FOR, companyCellParser));
        cellBinderRegistry.setCellBinder(optional(StaffFields.POST));
        cellBinderRegistry.setCellBinder(optional(StaffFields.DIVISION, divisionCellParser));
        cellBinderRegistry.setCellBinder(optional(StaffFields.WORKSTATION, workstationCellParser));
        cellBinderRegistry.setCellBinder(optional(StaffFields.CREW, crewCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
