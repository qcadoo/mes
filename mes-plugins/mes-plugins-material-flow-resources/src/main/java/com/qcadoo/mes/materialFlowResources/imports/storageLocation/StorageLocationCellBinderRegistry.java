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
package com.qcadoo.mes.materialFlowResources.imports.storageLocation;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class StorageLocationCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser locationCellParser;

    @Autowired
    private CellParser productCellParser;

    @Autowired
    private CellParser bigDecimalCellParser;

    @Autowired
    private CellParser booleanCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(StorageLocationFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(StorageLocationFields.LOCATION, locationCellParser));
        cellBinderRegistry.setCellBinder(optional(StorageLocationFields.PLACE_STORAGE_LOCATION, booleanCellParser));
        cellBinderRegistry.setCellBinder(optional(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(StorageLocationFields.HIGH_STORAGE_LOCATION, booleanCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
