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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.view.api.components.GridComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

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

    private void disableAttrValuesGrid(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if(Objects.nonNull(form.getEntityId())) {
            Entity productComponent = form.getEntity();
            Entity mo = productComponent.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
            String state = mo.getStringField(MasterOrderFields.STATE);
            if(MasterOrderState.DECLINED.getStringValue().equals(state) || MasterOrderState.COMPLETED.getStringValue().equals(state)) {
                GridComponent attrValuesGrid = (GridComponent) view.getComponentByReference("masterOrderProductAttrValues");
                attrValuesGrid.setEditable(false);
            }
        }

    }

    public void fillUnitField(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(MasterOrderProductFields.PRODUCT);
        Entity product = productField.getEntity();
        String unit = null;

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);

        }
        for (String reference : Arrays.asList("cumulatedOrderQuantityUnit", "masterOrderQuantityUnit",
                "producedOrderQuantityUnit", "leftToReleaseUnit", "quantityRemainingToOrderUnit",
                "quantityTakenFromWarehouseUnit")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }

    }

    private void fillQuantities(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productComponent = form.getEntity();
        if (productComponent.getId() != null) {
            Entity masterOrderProductDto = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                    MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO).get(productComponent.getId());
            FieldComponent cumulatedOrderQuantity = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY);
            cumulatedOrderQuantity.setFieldValue(numberService.format(masterOrderProductDto
                    .getDecimalField(MasterOrderPositionDtoFields.CUMULATED_MASTER_ORDER_QUANTITY)));
            cumulatedOrderQuantity.requestComponentUpdateState();

            FieldComponent leftToRelease = (FieldComponent) view.getComponentByReference(MasterOrderProductFields.LEFT_TO_RELASE);
            leftToRelease.setFieldValue(numberService.format(masterOrderProductDto
                    .getDecimalField(MasterOrderPositionDtoFields.LEFT_TO_RELEASE)));
            leftToRelease.requestComponentUpdateState();

            FieldComponent producedOrderQuantity = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.PRODUCED_ORDER_QUANTITY);
            producedOrderQuantity.setFieldValue(numberService.format(masterOrderProductDto
                    .getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY)));
            producedOrderQuantity.requestComponentUpdateState();

            FieldComponent quantityRemainingToOrder = (FieldComponent) view
                    .getComponentByReference(MasterOrderProductFields.QUANTITY_REMAINING_TO_ORDER);
            quantityRemainingToOrder.setFieldValue(numberService.format(masterOrderProductDto
                    .getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER)));
            quantityRemainingToOrder.requestComponentUpdateState();
        }
    }

    private void fillDefaultTechnology(final ViewDefinitionState view) {
        if (PluginUtils.isEnabled("goodFood")) {
            FieldComponent technology = (FieldComponent) view.getComponentByReference("technology");
            technology.setRequired(true);
            technology.requestComponentUpdateState();
        }
        LookupComponent productField = (LookupComponent) view.getComponentByReference("product");
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference("defaultTechnology");
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference("technology");

        Entity product = productField.getEntity();

        if (Objects.nonNull(product)) {
            FilterValueHolder holder = technologyLookup.getFilterValue();

            holder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());

            technologyLookup.setFilterValue(holder);

            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

            if (Objects.nonNull(defaultTechnology)) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnology, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);
                if (technologyLookup.getFieldValue() == null && (technologyLookup.getCurrentCode() == null
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

            if ((masterOrderProduct == null) || !masterOrderProduct.isValid()) {
                return;
            }

            if (Objects.nonNull(masterOrderProduct.getId())) {
                Entity masterOrderProductDto = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                        MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO).get(masterOrderProduct.getId());
                if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(masterOrderProductDto
                        .getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER))) < 0) {
                    masterOrderProductForm.addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                            MessageType.INFO, false);
                }
            }
        }
    }

}
