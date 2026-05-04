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
package com.qcadoo.mes.productionCounting.imports.productionTracking;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.imports.parsers.CausesOfWastesCellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class ProductionTrackingCellBinderRegistry {

    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser orderByBarcodeCellParser;

    @Autowired
    private CellParser staffCellParser;

    @Autowired
    private CellParser shiftCellParser;

    @Autowired
    private CellParser bigDecimalCellParser;

    @Autowired
    private CellParser dateTimeCellParser;

    @Autowired
    private CausesOfWastesCellParser causesOfWastesCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(ProductionTrackingFields.ORDER, orderByBarcodeCellParser));
        cellBinderRegistry.setCellBinder(required(ProductionTrackingFields.STAFF, staffCellParser));
        cellBinderRegistry.setCellBinder(required(ProductionTrackingFields.SHIFT, shiftCellParser));
        cellBinderRegistry.setCellBinder(required(ProductionTrackingFields.TIME_RANGE_FROM, dateTimeCellParser));
        cellBinderRegistry.setCellBinder(required(ProductionTrackingFields.TIME_RANGE_TO, dateTimeCellParser));
        cellBinderRegistry.setCellBinder(required(TrackingOperationProductOutComponentFields.USED_QUANTITY, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(TrackingOperationProductOutComponentFields.WASTES_QUANTITY, bigDecimalCellParser));
        cellBinderRegistry.setCellBinder(optional(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES, causesOfWastesCellParser.causesOfWastes()));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }

}
