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
package com.qcadoo.mes.basicProductionCounting.hooks;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class BasicProductionCountingDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_BASIC = "01basic";

    private static final String L_COMPONENT = "01component";

    private static final String L_FINAL_PRODUCT = "03finalProduct";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUnit";

    private static final String L_PRODUCED_QUANTITY_UNIT = "producedQuantityUnit";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private TechnologyService technologyService;

    public void disableUsedAndProducedFieldsDependsOfProductType(final ViewDefinitionState view) {
        FormComponent basicProductionCountingForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent producedQuantityField = (FieldComponent) view
                .getComponentByReference(BasicProductionCountingFields.PRODUCED_QUANTITY);
        FieldComponent usedQuantityField = (FieldComponent) view
                .getComponentByReference(BasicProductionCountingFields.USED_QUANTITY);

        Long basicProductionCountingId = basicProductionCountingForm.getEntityId();

        if (basicProductionCountingId != null) {
            Entity basicProductionCounting = basicProductionCountingService.getBasicProductionCounting(basicProductionCountingId);

            Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);
            String state = order.getStringField(OrderFields.STATE);
            String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);

            if (L_BASIC.equals(typeOfProductionRecording)
                    && (OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED.equals(state))) {
                if (L_FINAL_PRODUCT.equals(technologyService.getProductType(product, technology))) {
                    usedQuantityField.setEnabled(false);
                } else {
                    usedQuantityField.setEnabled(true);
                }

                if (L_COMPONENT.equals(technologyService.getProductType(product, technology))) {
                    producedQuantityField.setEnabled(false);
                } else {
                    producedQuantityField.setEnabled(true);
                }
            } else {
                usedQuantityField.setEnabled(false);
                producedQuantityField.setEnabled(false);
            }
        }
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FormComponent basicProductionCountingForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long basicProductionCountingId = basicProductionCountingForm.getEntityId();

        if (basicProductionCountingId == null) {
            return;
        }

        Entity basicProductionCountingEntity = basicProductionCountingService
                .getBasicProductionCounting(basicProductionCountingId);

        Entity product = basicProductionCountingEntity.getBelongsToField(BasicProductionCountingFields.PRODUCT);

        if (product == null) {
            return;
        }

        for (String reference : Arrays.asList(L_PLANNED_QUANTITY_UNIT, L_USED_QUANTITY_UNIT, L_PRODUCED_QUANTITY_UNIT)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getField(ProductFields.UNIT));
            field.requestComponentUpdateState();
        }
    }

}
