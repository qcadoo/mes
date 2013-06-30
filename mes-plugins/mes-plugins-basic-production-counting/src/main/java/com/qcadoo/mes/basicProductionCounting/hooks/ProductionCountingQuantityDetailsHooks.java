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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductionCountingQuantityDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_FOR_EACH = "03forEach";

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity productionCountingQuantity = productionCountingQuantityForm.getEntity();

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity basicProductionCounting = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

        if (order != null) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology != null) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);

                return;
            }
        }

        if (basicProductionCounting != null) {
            Entity basicProductionCountingOrder = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);

            if (basicProductionCountingOrder != null) {
                Entity technology = basicProductionCountingOrder.getBelongsToField(OrderFields.TECHNOLOGY);

                if (technology != null) {
                    FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                    filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                    technologyOperationComponentLookup.setFilterValue(filterValueHolder);

                    return;
                }
            }
        }
    }

    public void disableFieldsDependsOfState(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductionCountingQuantityFields.PRODUCT);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent typeOfMaterialField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);

        Long productionCountingQuantityId = productionCountingQuantityForm.getEntityId();

        boolean isEnabled = (productionCountingQuantityId == null);

        updateFieldComponentState(productLookup, isEnabled);
        updateFieldComponentState(technologyOperationComponentLookup, isEnabled);
        updateFieldComponentState(typeOfMaterialField, isEnabled);
        updateFieldComponentState(roleField, isEnabled);
    }

    private void updateFieldComponentState(final FieldComponent filedComponent, final boolean isEnabled) {
        filedComponent.setEnabled(isEnabled);
        filedComponent.requestComponentUpdateState();
    }

    public void fillProductField(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(ProductionCountingQuantityFields.PRODUCT);

        Long productionCountingQuantityId = productionCountingQuantityForm.getEntityId();

        if (productionCountingQuantityId != null) {
            return;
        }

        Entity productionCountingQuantity = productionCountingQuantityForm.getEntity();

        Entity basicProductionCounting = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

        if (basicProductionCounting != null) {
            Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);

            if (product != null) {
                productLookup.setFieldValue(product.getId());
                productLookup.setEnabled(false);
                productLookup.requestComponentUpdateState();
            }
        }
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT);

        basicProductionCountingService.fillUnitFields(view, ProductionCountingQuantityFields.PRODUCT, referenceNames);
    }

    public void setOperationFieldRequiredDependsOfOrder(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity productionCountingQuantity = productionCountingQuantityForm.getEntity();

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity basicProductionCounting = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

        if (order != null) {
            String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

            if (L_FOR_EACH.equals(typeOfProductionRecording)) {
                technologyOperationComponentLookup.setRequired(true);

                return;
            }
        }

        if (basicProductionCounting != null) {
            Entity basicProductionCountingOrder = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);

            if (basicProductionCountingOrder != null) {
                String typeOfProductionRecording = basicProductionCountingOrder.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

                if (L_FOR_EACH.equals(typeOfProductionRecording)) {
                    technologyOperationComponentLookup.setRequired(true);

                    return;
                }
            }
        }

        technologyOperationComponentLookup.setRequired(false);
    }

}
