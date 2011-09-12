/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.productionTimeNorms;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.productionTimeNorms.TimeNormsConstants.*;
import static com.qcadoo.view.api.ComponentState.MessageType.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class NormService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionInOneCycle");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");
        Object value = countRealized.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        countRealized.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealized.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
    }

    public void updateFieldsStateWhenDefaultValueCheckboxChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        /* uruchamia sie hook before render */
    }

    public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if (countRealized.getFieldValue().equals("02specified")) {
            countMachine.setVisible(true);
            countMachine.setEnabled(true);
        } else {
            countMachine.setVisible(false);
        }
    }

    public void copyTimeNormsFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
            final String[] args) {

        if (operationLookupState.getFieldValue() == null) {
            view.getComponentByReference("form")
                    .addMessage(
                            translationService.translate("productionTimeNorms.messages.info.missingOperationReference",
                                    view.getLocale()), INFO);
            return;
        }

        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookupState.getFieldValue());

        applyCostNormsFromGivenSource(view, operation, FIELDS_OPERATION);
    }

    public void copyTimeNormsFromTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity orderOperationComponent = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // be sure that entity isn't in detached state
        orderOperationComponent = orderOperationComponent.getDataDefinition().get(orderOperationComponent.getId());

        applyCostNormsFromGivenSource(view, orderOperationComponent.getBelongsToField("technologyOperationComponent"),
                FIELDS_TECHNOLOGY);
    }

    private void applyCostNormsFromGivenSource(final ViewDefinitionState view, final Entity source, final Iterable<String> fields) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : fields) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }

        if (source.getField("countRealized") == null) {
            view.getComponentByReference("countRealized").setFieldValue("01all");
        }

        if (source.getField("productionInOneCycle") == null) {
            view.getComponentByReference("productionInOneCycle").setFieldValue("1");
        }

        // FIXME MAKU fix problem with double notifications after operation changed
        // view.getComponentByReference("form").addMessage(translationService.translate("productionTimeNorms.messages.success.copyTimeNormsSuccess",
        // view.getLocale()), SUCCESS);
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if ("02specified".equals(countRealizedOperation.getFieldValue())) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
        }
    }

    public void inheritOperationNormValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        copyTimeNormsFromOperation(viewDefinitionState, componentState, args);
    }

}
