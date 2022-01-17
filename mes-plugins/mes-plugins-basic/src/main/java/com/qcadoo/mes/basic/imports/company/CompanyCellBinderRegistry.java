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
package com.qcadoo.mes.basic.imports.company;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

@Component
public class CompanyCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser countryCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(CompanyFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(CompanyFields.NAME));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.TAX));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.COUNTRY, countryCellParser));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.STREET));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.HOUSE));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.FLAT));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.ZIP_CODE));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.CITY));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.STATE));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.PHONE));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.EMAIL));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.WEBSITE));
        cellBinderRegistry.setCellBinder(optional(CompanyFields.CONTACT_PERSON));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
