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

        if (qualityControlType != null && qualityControlType.equals("02forUnit")) {

            if (unitSamplingNr == null) {
                entity.addError(dataDefinition.getField(L_UNIT_SAMPLING_NR),
                        "technologies.technology.validate.global.error.unitSamplingNr");
                return false;
            }
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
