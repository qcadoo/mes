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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.model.api.*;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

@Service
public class MasterOrderProductDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillUnitField(view);
        fillDefaultTechnology(view);
        showErrorWhenCumulatedQuantity(view);
        fillQuantities(view);
        disableAttrValuesGrid(view);
    }

    public void fillUnitField(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(MasterOrderProductFields.PRODUCT);

        Entity product = productField.getEntity();
        String unit = null;

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        for (String reference : Arrays.asList("cumulatedOrderQuantityUnit", "masterOrderQuantityUnit",
                "producedOrderQuantityUnit", "leftToReleaseUnit", "quantityRemainingToOrderUnit",
                "quantityTakenFromWarehouseUnit")) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setFieldValue(unit);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private void fillDefaultTechnology(final ViewDefinitionState view) {
        if (PluginUtils.isEnabled("goodFood")) {
            FieldComponent technologyField = (FieldComponent) view.getComponentByReference(MasterOrderProductFields.TECHNOLOGY);

            technologyField.setRequired(true);
            technologyField.requestComponentUpdateState();
        }

        LookupComponent productField = (LookupComponent) view.getComponentByReference(MasterOrderProductFields.PRODUCT);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(MasterOrderProductFields.DEFAULT_TECHNOLOGY);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(MasterOrderProductFields.TECHNOLOGY);

        Entity product = productField.getEntity();

        if (Objects.nonNull(product)) {
            FilterValueHolder filterValueHolder = technologyLookup.getFilterValue();

            filterValueHolder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());

            technologyLookup.setFilterValue(filterValueHolder);

            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

            if (Objects.nonNull(defaultTechnology)) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnology, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);

                if (Objects.isNull(technologyLookup.getFieldValue()) && (Objects.isNull(technologyLookup.getCurrentCode())
                        || technologyLookup.getCurrentCode().isEmpty())) {
                    technologyLookup.setFieldValue(defaultTechnology.getId());
                }
            }
        }
    }

    private void showErrorWhenCumulatedQuantity(final ViewDefinitionState view) {
        if (view.isViewAfterRedirect()) {
            FormComponent masterOrderProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            
            Entity masterOrderProduct = masterOrderProductForm.getPersistedEntityWithIncludedFormValues();

            if (Objects.isNull(masterOrderProduct) || !masterOrderProduct.isValid()) {
                return;
            }

            if (Objects.nonNull(masterOrderProduct.getId())) {
                Entity masterOrderPositionDto = getMasterOrderPositionDtoDD().get(masterOrderProduct.getId());

                if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(masterOrderPositionDto
                        .getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER))) < 0) {
                    masterOrderProductForm.addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                            MessageType.INFO, false);
                }
            }
        }
    }

    private void fillQuantities(final ViewDefinitionState view) {
        FormComponent masterOrderProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long masterOrderProductFormId = masterOrderProductForm.getEntityId();

        if (Objects.nonNull(masterOrderProductFormId)) {
            Entity masterOrderPositionDto = getMasterOrderPositionDtoDD().get(masterOrderProductFormId);

            FieldComponent cumulatedOrderQuantity = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY);
            cumulatedOrderQuantity.setFieldValue(numberService.format(masterOrderPositionDto
                    .getDecimalField(MasterOrderPositionDtoFields.CUMULATED_MASTER_ORDER_QUANTITY)));
            cumulatedOrderQuantity.requestComponentUpdateState();

            FieldComponent leftToRelease = (FieldComponent) view.getComponentByReference(MasterOrderProductFields.LEFT_TO_RELEASE);
            leftToRelease.setFieldValue(numberService.format(masterOrderPositionDto
                    .getDecimalField(MasterOrderPositionDtoFields.LEFT_TO_RELEASE)));
            leftToRelease.requestComponentUpdateState();

            FieldComponent producedOrderQuantity = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.PRODUCED_ORDER_QUANTITY);
            producedOrderQuantity.setFieldValue(numberService.format(masterOrderPositionDto
                    .getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY)));
            producedOrderQuantity.requestComponentUpdateState();

            FieldComponent quantityRemainingToOrder = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.QUANTITY_REMAINING_TO_ORDER);
            quantityRemainingToOrder.setFieldValue(numberService.format(masterOrderPositionDto
                    .getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER)));
            quantityRemainingToOrder.requestComponentUpdateState();
        }
    }

    private void disableAttrValuesGrid(final ViewDefinitionState view) {
        FormComponent masterOrderProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long masterOrderProductFormId = masterOrderProductForm.getEntityId();

        if (Objects.nonNull(masterOrderProductFormId)) {
            Entity masterOrderProduct = masterOrderProductForm.getPersistedEntityWithIncludedFormValues();

            Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
            String state = masterOrder.getStringField(MasterOrderFields.STATE);

            if (MasterOrderState.DECLINED.getStringValue().equals(state) || MasterOrderState.COMPLETED.getStringValue().equals(state)) {
                GridComponent attrValuesGrid = (GridComponent) view.getComponentByReference(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES);
                attrValuesGrid.setEditable(false);
            }
        }
    }

    private DataDefinition getMasterOrderPositionDtoDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO);
    }

}
