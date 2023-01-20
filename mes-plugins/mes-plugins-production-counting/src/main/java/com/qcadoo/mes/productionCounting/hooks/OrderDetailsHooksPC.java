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

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderDetailsHooksPC {

    private static final List<String> L_ORDER_FIELD_NAMES = Lists.newArrayList(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.REGISTER_PRODUCTION_TIME);

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ParameterService parameterService;

    public void checkTypeOfProductionRecording(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        String typeOfProductionRecording = (String) typeOfProductionRecordingField.getFieldValue();

        if (StringUtils.isEmpty(typeOfProductionRecording)
                || productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)) {
            productionCountingService.setComponentsState(view, L_ORDER_FIELD_NAMES, false, true);
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity order = form.getPersistedEntityWithIncludedFormValues();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology) && technology.getBooleanField(TechnologyFieldsPC.PIECEWORK_PRODUCTION)) {
            typeOfProductionRecordingField.setEnabled(false);
        } else if (Objects.nonNull(technology)) {
            for (Entity toc : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
                if (toc.getBooleanField(TechnologyOperationComponentFieldsTNFO.PIECEWORK_PRODUCTION)) {
                    typeOfProductionRecordingField.setEnabled(false);
                    break;
                }
            }
        }
    }

    public void changeDoneQuantityAndAmountOfProducedQuantityFieldState(final ViewDefinitionState view) {
        productionCountingService.changeDoneQuantityAndAmountOfProducedQuantityFieldState(view);
    }

}
