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
package com.qcadoo.mes.techSubcontracting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechSubcontractingService {

    private static final String OPERATION_FIELD = "operation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void inheritSubcontractingValue(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        ComponentState operationLookup = view.getComponentByReference(OPERATION_FIELD);
        if (operationLookup.getFieldValue() == null) {
            return;
        }

        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookup.getFieldValue());

        checkArgument(operation != null, "source entity is null");

        FieldComponent isSucontractungComponent = (FieldComponent) view.getComponentByReference(IS_SUBCONTRACTING);
        isSucontractungComponent.setFieldValue(operation.getField(IS_SUBCONTRACTING));

    }
}
