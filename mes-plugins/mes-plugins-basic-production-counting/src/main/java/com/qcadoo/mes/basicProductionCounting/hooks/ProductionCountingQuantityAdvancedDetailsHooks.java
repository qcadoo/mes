/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityDtoFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionCountingQuantityAdvancedDetailsHooks {

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUnit";

    private static final String L_PRODUCED_QUANTITY_UNIT = "producedQuantityUnit";

    private static final String L_USED_QUANTITY_GRID_LAYOUT = "usedQuantityGridLayout";

    private static final String L_PRODUCED_QUANTITY_GRID_LAYOUT = "producedQuantityGridLayout";

    public static final String ATTRIBUTES = "attributes";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setCriteriaModifierParameters(view);
        disableFieldsDependsOfState(view);
        hideFieldsDependsOfState(view);
        fillProductField(view);
        fillUnitFields(view);
        setTechnologyOperationComponentFieldRequired(view);
        hideTab(view);

        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (productionCountingQuantityForm.getEntityId() != null) {
            Entity productionCountingQuantityDto = dataDefinitionService
                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY_DTO)
                    .get(productionCountingQuantityForm.getEntityId());
            FieldComponent usedQuantity = (FieldComponent) view
                    .getComponentByReference(ProductionCountingQuantityDtoFields.USED_QUANTITY);
            FieldComponent producedQuantity = (FieldComponent) view
                    .getComponentByReference(ProductionCountingQuantityDtoFields.PRODUCED_QUANTITY);
            usedQuantity.setFieldValue(
                    productionCountingQuantityDto.getDecimalField(ProductionCountingQuantityDtoFields.USED_QUANTITY));
            producedQuantity.setFieldValue(
                    productionCountingQuantityDto.getDecimalField(ProductionCountingQuantityDtoFields.PRODUCED_QUANTITY));
        }
    }

    private void hideTab(ViewDefinitionState view) {
        ComponentState attributesTab = view.getComponentByReference(ATTRIBUTES);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);
        boolean isProduced = checkIfIsProduced((String) roleField.getFieldValue());
        attributesTab.setVisible(isProduced);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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
                }
            }
        }
    }

    private void disableFieldsDependsOfState(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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

    private void hideFieldsDependsOfState(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        ComponentState usedQuantityGridLayout = view.getComponentByReference(L_USED_QUANTITY_GRID_LAYOUT);
        ComponentState producedQuantityGridLayout = view.getComponentByReference(L_PRODUCED_QUANTITY_GRID_LAYOUT);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);

        Long productionCountingQuantityId = productionCountingQuantityForm.getEntityId();

        boolean isEnabled = (productionCountingQuantityId != null);
        boolean isUsed = checkIfIsUsed((String) roleField.getFieldValue());
        boolean isProduced = checkIfIsProduced((String) roleField.getFieldValue());

        updateComponentState(usedQuantityGridLayout, isEnabled && isUsed);
        updateComponentState(producedQuantityGridLayout, isEnabled && isProduced);
    }

    private boolean checkIfIsUsed(final String role) {
        return (StringUtils.isNotEmpty(role) && ProductionCountingQuantityRole.USED.getStringValue().equals(role));
    }

    private boolean checkIfIsProduced(final String role) {
        return (StringUtils.isNotEmpty(role) && ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role));
    }

    private void updateComponentState(final ComponentState componentState, final boolean isVisible) {
        componentState.setVisible(isVisible);
    }

    private void fillProductField(final ViewDefinitionState view) {
        FormComponent productionCountingQuantityForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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

    private void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT, L_USED_QUANTITY_UNIT, L_PRODUCED_QUANTITY_UNIT);

        basicProductionCountingService.fillUnitFields(view, ProductionCountingQuantityFields.PRODUCT, referenceNames);
    }

    private void setTechnologyOperationComponentFieldRequired(final ViewDefinitionState view) {
        basicProductionCountingService.setTechnologyOperationComponentFieldRequired(view);
    }

}
