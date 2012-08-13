/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.qualityControls;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechnologyModelValidatorQC {

    private static final String L_QUALITY_CONTROL_TYPE = "qualityControlType";

    private static final String L_UNIT_SAMPLING_NR = "unitSamplingNr";

    public boolean checkIfUnitSampligNrIsReq(final DataDefinition dataDefinition, final Entity entity) {
        String qualityControlType = (String) entity.getField(L_QUALITY_CONTROL_TYPE);
        BigDecimal unitSamplingNr = (BigDecimal) entity.getField(L_UNIT_SAMPLING_NR);

        if ((qualityControlType != null) && "02forUnit".equals(qualityControlType) && (unitSamplingNr == null)) {
            entity.addError(dataDefinition.getField(L_UNIT_SAMPLING_NR),
                    "technologies.technology.validate.global.error.unitSamplingNr");
            return false;
        }
        return true;

    }

    public void checkQualityControlType(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            throw new IllegalStateException("component is not select");
        }

        FieldComponent qualityControlType = (FieldComponent) state;

        FieldComponent unitSamplingNr = (FieldComponent) viewDefinitionState.getComponentByReference(L_UNIT_SAMPLING_NR);

        if (qualityControlType.getFieldValue() != null) {
            if (qualityControlType.getFieldValue().equals("02forUnit")) {
                unitSamplingNr.setRequired(true);
                unitSamplingNr.setVisible(true);
            } else {
                unitSamplingNr.setFieldValue(null);
                unitSamplingNr.setRequired(false);
                unitSamplingNr.setVisible(false);
            }
        }
    }
}
