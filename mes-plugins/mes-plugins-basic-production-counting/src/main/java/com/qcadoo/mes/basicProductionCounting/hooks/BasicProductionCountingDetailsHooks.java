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

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.USED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class BasicProductionCountingDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_COMPONENT = "01component";

    private static final String L_FINAL_PRODUCT = "03finalProduct";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    public void shouldDisableUsedProducedField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent producedField = (FieldComponent) view.getComponentByReference(PRODUCED_QUANTITY);
        FieldComponent usedField = (FieldComponent) view.getComponentByReference(USED_QUANTITY);
        Long basicProductionCountingId = form.getEntityId();

        if (basicProductionCountingId != null) {
            final Entity basicProductionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(basicProductionCountingId);

            Entity order = basicProductionCounting.getBelongsToField(ORDER);
            Entity technology = order.getBelongsToField(TECHNOLOGY);
            Entity product = basicProductionCounting.getBelongsToField(PRODUCT);

            if (L_FINAL_PRODUCT.equals(technologyService.getProductType(product, technology))) {
                usedField.setEnabled(false);
            } else {
                usedField.setEnabled(true);

            }
            if (L_COMPONENT.equals(technologyService.getProductType(product, technology))) {
                producedField.setEnabled(false);
            } else {
                producedField.setEnabled(true);
            }
        }
    }

    public void getProductNameFromCounting(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent productField = (FieldComponent) view.getComponentByReference(PRODUCT);

        if (formComponent.getEntityId() == null) {
            return;
        }

        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(formComponent.getEntityId());

        if (basicProductionCountingEntity == null) {
            return;
        }

        Entity product = basicProductionCountingEntity.getBelongsToField(PRODUCT);
        productField.setFieldValue(product.getField(NAME));
        productField.requestComponentUpdateState();
    }

    public void fillFieldsCurrency(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntity() == null) {
            return;
        }

        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(form.getEntityId());

        Entity product = basicProductionCountingEntity.getBelongsToField(PRODUCT);

        if (product == null) {
            return;
        }

        for (String reference : Arrays.asList("usedQuantityCurrency", "producedQuantityCurrency", "plannedQuantityCurrency")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getField("unit"));
            field.requestComponentUpdateState();
        }
    }

}
