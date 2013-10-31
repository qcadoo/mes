/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionTrackingServiceImpl implements ProductionTrackingService {

    private static final String L_BORDER_LAYOUT_TIME = "borderLayoutTime";

    private static final String L_BORDER_LAYOUT_PIECEWORK = "borderLayoutPiecework";

    @Autowired
    private ProductionCountingService productionCountingService;

    @Override
    public void fillShiftAndDivisionField(final ViewDefinitionState view) {
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.STAFF);
        LookupComponent shiftLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.SHIFT);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity staff = staffLookup.getEntity();

        if (staff == null) {
            shiftLookup.setFieldValue(null);

            return;
        }

        Entity shift = staff.getBelongsToField(ProductionTrackingFields.SHIFT);

        if (shift == null) {
            shiftLookup.setFieldValue(null);
        } else {
            shiftLookup.setFieldValue(shift.getId());
        }

        Entity division = staff.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    @Override
    public void fillDivisionField(final ViewDefinitionState view) {
        LookupComponent workstationTypeLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.WORKSTATION_TYPE);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(ProductionTrackingFields.DIVISION);

        Entity workstationType = workstationTypeLookup.getEntity();

        if (workstationType == null) {
            divisionLookup.setFieldValue(null);

            return;
        }

        Entity division = workstationType.getBelongsToField(ProductionTrackingFields.DIVISION);

        if (division == null) {
            divisionLookup.setFieldValue(null);
        } else {
            divisionLookup.setFieldValue(division.getId());
        }
    }

    @Override
    public void setTimeAndPieceworkComponentsVisible(final ViewDefinitionState view, final Entity order) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        ComponentState borderLayoutTime = view.getComponentByReference(L_BORDER_LAYOUT_TIME);
        ComponentState borderLayoutPiecework = view.getComponentByReference(L_BORDER_LAYOUT_PIECEWORK);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        boolean registerProductionTime = order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME);
        boolean registerPiecework = order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK);

        boolean isBasic = productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording);
        boolean isForEach = productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording);

        technologyOperationComponentLookup.setVisible(isForEach);
        technologyOperationComponentLookup.requestComponentUpdateState();

        borderLayoutTime.setVisible(registerProductionTime && !isBasic);
        borderLayoutPiecework.setVisible(registerPiecework && isForEach);
    }

}
