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
package com.qcadoo.mes.productFlowThruDivision.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.DivisionFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.hooks.ProductionCountingQuantityAdvancedDetailsHooksBPC;
import com.qcadoo.mes.productFlowThruDivision.hooks.TechnologyHooksPFTD;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionCountingQuantityAdvancedDetailsListenersPFTD {

    @Autowired private ProductionCountingQuantityAdvancedDetailsHooksBPC productionCountingQuantityAdvancedDetailsHooksBPC;

    @Autowired private TechnologyHooksPFTD technologyHooksPFTD;

    public void setFlowTabState(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        productionCountingQuantityAdvancedDetailsHooksBPC.setFlowTabState(view);
    }

    public void onProductionFlowComponentChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        FieldComponent flowField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW);
        String flowType = (String) flowField.getFieldValue();

        if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(flowType)) {
            productsFlowLocationLookup.setEnabled(true);
        } else {
            productsFlowLocationLookup.setEnabled(false);
        }
    }

    public void onChangeTOC(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        LookupComponent tocComponent = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        Entity technology = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER)
                .getBelongsToField(OrderFields.TECHNOLOGY);
        String range = technology.getStringField(TechnologyFieldsPFTD.RANGE);
        Entity toc = tocComponent.getEntity();
        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);
        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        FieldComponent select = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW);
        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        if (toc != null) {
            if (Range.MANY_DIVISIONS.getStringValue().equals(range)) {
                Entity division = toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
                if (division != null) {
                    if (division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_LOCATION) != null) {
                        componentsLocationLookup
                                .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_LOCATION).getId());
                        componentsLocationLookup.requestComponentUpdateState();
                    }
                    if (division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_OUTPUT_LOCATION) != null) {
                        componentsOutputLocationLookup
                                .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                        componentsOutputLocationLookup.requestComponentUpdateState();
                    }
                    select.setFieldValue(division.getField(DivisionFieldsPFTD.PRODUCTION_FLOW));
                    select.requestComponentUpdateState();
                    if (division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {

                        productsFlowLocationLookup
                                .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                        productsFlowLocationLookup.requestComponentUpdateState();
                    }
                    if (division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_INPUT_LOCATION) != null) {

                        productsInputLocationLookup
                                .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                        productsInputLocationLookup.requestComponentUpdateState();
                    }
                }
            }
        }
        ent.getFields();
    }
}
