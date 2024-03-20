/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductionCountingQuantityAdvancedDetailsHooksBPC {

    @Autowired
    private NumberService numberService;

    private List<String> orderStatusesToEdit = Lists.newArrayList(OrderStateStringValues.PENDING, OrderStateStringValues.ACCEPTED, OrderStateStringValues.IN_PROGRESS);

    public void onBeforeRender(final ViewDefinitionState view) {
        setFlowTabState(view);


        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity pcq = form.getPersistedEntityWithIncludedFormValues();
        Entity order = pcq.getBelongsToField(ProductionCountingQuantityFields.ORDER);


        ComponentState orderProductResourceReservationsTab = view.getComponentByReference("productResourceReservations");
        orderProductResourceReservationsTab.setVisible(ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(pcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)));

        if (Objects.isNull(pcq.getId())) {
            return;
        }

        Entity product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        ComponentState productNumber = view.getComponentByReference("resourceProductNumber");
        productNumber.setFieldValue(product.getStringField(ProductFields.NUMBER));

        ComponentState resourcePlannedQuantity = view.getComponentByReference("resourcePlannedQuantity");
        resourcePlannedQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY), 0));

        ComponentState resourcePlannedQuantityUnit = view.getComponentByReference("resourcePlannedQuantityUnit");
        resourcePlannedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));

        GridComponent orderProductResourceReservationsGrid = (GridComponent) view.getComponentByReference("orderProductResourceReservations");
        if (!orderStatusesToEdit.contains(order.getStringField(OrderFields.STATE))) {
            orderProductResourceReservationsGrid.setEditable(false);
        }
    }

    public void setFlowTabState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);
        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        LookupComponent wasteReceptionWarehouseLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);

        FieldComponent productionFlowSelect = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW);
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);
        FieldComponent typeField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        String role = (String) roleField.getFieldValue();
        String type = (String) typeField.getFieldValue();

        componentsLocationLookup.setEnabled(false);
        componentsLocationLookup.setRequired(false);
        componentsLocationLookup.requestComponentUpdateState();
        componentsOutputLocationLookup.setEnabled(false);
        componentsOutputLocationLookup.requestComponentUpdateState();
        productsInputLocationLookup.setEnabled(false);
        productsInputLocationLookup.setRequired(false);
        productsInputLocationLookup.requestComponentUpdateState();
        productionFlowSelect.setEnabled(false);
        productionFlowSelect.requestComponentUpdateState();
        productsFlowLocationLookup.setEnabled(false);
        productsFlowLocationLookup.requestComponentUpdateState();

        wasteReceptionWarehouseLookup.setEnabled(false);
        wasteReceptionWarehouseLookup.requestComponentUpdateState();

        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)
                || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))) {
            productsInputLocationLookup.setEnabled(true);
            productsInputLocationLookup.setRequired(true);
            productsInputLocationLookup.requestComponentUpdateState();
        } else if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type))) {
            wasteReceptionWarehouseLookup.setEnabled(true);
            wasteReceptionWarehouseLookup.requestComponentUpdateState();
        } else if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
            componentsLocationLookup.setEnabled(true);
            componentsLocationLookup.setRequired(true);
            componentsLocationLookup.requestComponentUpdateState();
            componentsOutputLocationLookup.setEnabled(true);
            componentsOutputLocationLookup.requestComponentUpdateState();
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
            productionFlowSelect.setEnabled(true);
            productionFlowSelect.requestComponentUpdateState();
        }

        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        Entity order = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER);

        if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)
                && TypeOfProductionRecording.CUMULATED.getStringValue()
                .equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            productionFlowSelect.setFieldValue(ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
            productionFlowSelect.setEnabled(false);
            productionFlowSelect.requestComponentUpdateState();
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();
        }

        productsFlowLocationLookup.setEnabled(ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlowSelect.getFieldValue()));
    }
}
