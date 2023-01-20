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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologyOperationComponentDetailsHooksPC {

    @Autowired
    private ProductionCountingService productionCountingService;

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent pieceworkProduction = (FieldComponent) viewDefinitionState.getComponentByReference(TechnologyOperationComponentFieldsTNFO.PIECEWORK_PRODUCTION);

        pieceworkProduction.setEnabled(productionCountingService.isTypeOfProductionRecordingForEach(form.getPersistedEntityWithIncludedFormValues()
                .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY)
                .getStringField(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING)));
    }
}
