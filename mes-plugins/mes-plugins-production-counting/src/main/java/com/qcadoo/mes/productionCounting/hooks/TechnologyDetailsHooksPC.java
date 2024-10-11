/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TechnologyDetailsHooksPC {

    private static final String WORKSTATIONS = "workstations";
    private static final String WORKSTATIONS_TECHNOLOGY_OPERATION_COMPONENT = "workstationsTechnologyOperationComponent";
    private static final String OPERATION_WORKSTATIONS_DESCRIPTION = "operationWorkstationsDescription";

    private static final List<String> L_TECHNOLOGY_FIELD_NAMES = Lists.newArrayList(
            TechnologyFieldsPC.REGISTER_QUANTITY_IN_PRODUCT, TechnologyFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT,
            TechnologyFieldsPC.REGISTER_PRODUCTION_TIME);

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ParameterService parameterService;

    public void setTechnologyDefaultValues(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (Objects.nonNull(form.getEntityId())) {
            return;
        }

        for (String fieldComponentName : L_TECHNOLOGY_FIELD_NAMES) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldComponentName);

            if (Objects.isNull(fieldComponent.getFieldValue())) {
                fieldComponent.setFieldValue(getDefaultValueForProductionCountingFromParameter(fieldComponentName));
                fieldComponent.requestComponentUpdateState();
            }

            fieldComponent.setEnabled(false);
        }

        if (Objects.isNull(typeOfProductionRecordingField.getFieldValue())) {
            typeOfProductionRecordingField.setFieldValue(
                    getDefaultValueForTypeOfProductionRecordingParameter(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        }
    }

    private void hideWorkstationsTableForCumulatedProductionRecording(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingFieldComponent = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        GridComponent workstationsTechnologyOperationComponent = (GridComponent) view
                .getComponentByReference(WORKSTATIONS_TECHNOLOGY_OPERATION_COMPONENT);
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(WORKSTATIONS);
        ComponentState operationWorkstationsDescriptionLabel = view.getComponentByReference(OPERATION_WORKSTATIONS_DESCRIPTION);

        if (Objects.nonNull(typeOfProductionRecordingFieldComponent) && TypeOfProductionRecording.FOR_EACH.getStringValue()
                .equals(typeOfProductionRecordingFieldComponent.getFieldValue())) {
            workstationsTechnologyOperationComponent.setVisible(true);
            workstations.setVisible(true);
            operationWorkstationsDescriptionLabel.setVisible(true);
        } else {
            workstationsTechnologyOperationComponent.setVisible(false);
            workstations.setVisible(false);
            operationWorkstationsDescriptionLabel.setVisible(false);
        }
    }

    private boolean getDefaultValueForProductionCountingFromParameter(final String fieldName) {
        return parameterService.getParameter().getBooleanField(fieldName);
    }

    private String getDefaultValueForTypeOfProductionRecordingParameter(final String fieldName) {
        return parameterService.getParameter().getStringField(fieldName);
    }

    public void checkTypeOfProductionRecording(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent stateField = (FieldComponent) view
                .getComponentByReference(TechnologyFields.STATE);
        CheckBoxComponent pieceworkProductionField = (CheckBoxComponent) view.getComponentByReference(TechnologyFieldsPC.PIECEWORK_PRODUCTION);
        FieldComponent pieceRateField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPC.PIECE_RATE);
        String typeOfProductionRecording = (String) typeOfProductionRecordingField.getFieldValue();

        if (StringUtils.isEmpty(typeOfProductionRecording)
                || productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)) {
            productionCountingService.setComponentsState(view, L_TECHNOLOGY_FIELD_NAMES, false, true);
            pieceworkProductionField.setEnabled(false);
            pieceworkProductionField.setFieldValue(false);
        } else if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            pieceworkProductionField.setEnabled(false);
            pieceworkProductionField.setFieldValue(false);
        }
        if (pieceworkProductionField.isChecked() && TechnologyState.DRAFT.getStringValue().equals(stateField.getFieldValue())) {
            pieceRateField.setEnabled(true);
        } else if (pieceworkProductionField.isChecked() && !TechnologyState.DRAFT.getStringValue().equals(stateField.getFieldValue())) {
            pieceRateField.setEnabled(false);
        } else {
            pieceRateField.setEnabled(false);
            pieceRateField.setFieldValue(null);
        }
        hideWorkstationsTableForCumulatedProductionRecording(view);
    }

}
