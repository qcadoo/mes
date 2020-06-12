/**
 * ***************************************************************************
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
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class TechnologiesParametersHooksPFTD {

    public void onBeforeRender(final ViewDefinitionState view) {
        setDivisionField(view);
    }

    public void setDivisionField(final ViewDefinitionState view) {
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(ParameterFieldsPFTD.RANGE);
        LookupComponent divisionField = (LookupComponent) view.getComponentByReference(ParameterFieldsPFTD.DIVISION);

        String range = (String) rangeField.getFieldValue();

        boolean isOneDivision = Range.ONE_DIVISION.getStringValue().equals(range);
        boolean isManyDivisions = Range.MANY_DIVISIONS.getStringValue().equals(range);

        if (isManyDivisions) {
            divisionField.setFieldValue(null);
        }

        divisionField.setVisible(isOneDivision);
        divisionField.requestComponentUpdateState();
    }

}
