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
package com.qcadoo.mes.technologies.imports.workstationChangeoverNorm;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class WorkstationChangeoverNormCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser workstationTypeCellParser;

    @Autowired
    private CellParser workstationCellParser;

    @Autowired
    private CellParser attributeCellParser;

    @Autowired
    private CellParser workstationChangeoverTypeCellParser;

    @Autowired
    private CellParser attributeValueCellParser;

    @Autowired
    private CellParser timeCellParser;

    @Autowired
    private CellParser booleanCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.NAME));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.WORKSTATION_TYPE, workstationTypeCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.WORKSTATION, workstationCellParser));
        cellBinderRegistry.setCellBinder(required(WorkstationChangeoverNormFields.ATTRIBUTE, attributeCellParser));
        cellBinderRegistry.setCellBinder(required(WorkstationChangeoverNormFields.CHANGEOVER_TYPE, workstationChangeoverTypeCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE, WorkstationChangeoverNormFields.ATTRIBUTE, attributeValueCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE, WorkstationChangeoverNormFields.ATTRIBUTE, attributeValueCellParser));
        cellBinderRegistry.setCellBinder(required(WorkstationChangeoverNormFields.DURATION, timeCellParser));
        cellBinderRegistry.setCellBinder(optional(WorkstationChangeoverNormFields.IS_PARALLEL, booleanCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
