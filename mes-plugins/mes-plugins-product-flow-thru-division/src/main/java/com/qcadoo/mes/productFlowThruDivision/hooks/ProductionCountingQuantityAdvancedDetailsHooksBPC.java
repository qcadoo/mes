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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.DivisionFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.FlowTypeInComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.FlowTypeOutComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionCountingQuantityAdvancedDetailsHooksBPC {

    public void setFlowTabState(final ViewDefinitionState view) {
        FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);
        FieldComponent typeField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        String role = (String) roleField.getFieldValue();
        String type = (String) typeField.getFieldValue();

        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)) {
            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION, true);
            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION, false);
        } else if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION, false);
            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION, true);
        } else {

            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION, false);
            setLookupRequired(view, ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION, false);
        }
        fillWarehouses(view);
    }

    private void fillWarehouses(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        if (ent.getId() == null) {
            Entity order = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER);

            if (order == null) {
                return;
            }

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            String range = technology.getStringField(TechnologyFieldsPFTD.RANGE);
            LookupComponent tocComponent = (LookupComponent) view
                    .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
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

            if (Range.ONE_DIVISION.getStringValue().equals(range)) {
                if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION) != null) {
                    componentsLocationLookup
                            .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION).getId());
                    componentsLocationLookup.requestComponentUpdateState();
                }
                if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION) != null) {
                    componentsOutputLocationLookup
                            .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                    componentsOutputLocationLookup.requestComponentUpdateState();
                }
                select.setFieldValue(technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
                select.requestComponentUpdateState();

                Entity productsFlowLocation = technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                if (productsFlowLocation != null) {
                    productsFlowLocationLookup.setFieldValue(productsFlowLocation.getId());
                    productsFlowLocationLookup.requestComponentUpdateState();
                }

                productsInputLocationLookup
                        .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                productsInputLocationLookup.requestComponentUpdateState();
            } else if (Range.MANY_DIVISIONS.getStringValue().equals(range)) {
                if (toc != null) {
                    Entity division = toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
                    if (division != null) {
                        if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION) != null) {
                            componentsLocationLookup
                                    .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_LOCATION).getId());
                            componentsLocationLookup.requestComponentUpdateState();
                        }
                        if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION) != null) {
                            componentsOutputLocationLookup.setFieldValue(
                                    division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                            componentsOutputLocationLookup.requestComponentUpdateState();
                        }
                        select.setFieldValue(division.getField(DivisionFieldsPFTD.PRODUCTION_FLOW));
                        select.requestComponentUpdateState();
                        if (technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {
                            productsFlowLocationLookup
                                    .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                            productsFlowLocationLookup.requestComponentUpdateState();
                        }
                        if (technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION) != null) {
                            productsInputLocationLookup.setFieldValue(
                                    division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                            productsInputLocationLookup.requestComponentUpdateState();
                        }
                    }
                }
            }
        }

    }

    private void setLookupRequired(final ViewDefinitionState view, final String fieldName, final boolean required) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(fieldName);
        lookup.setRequired(required);
        lookup.requestComponentUpdateState();
    }

    private void fillLocation(final ViewDefinitionState view, final FieldComponent flowTypeInComponent,
            final LookupComponent componentsLocation, final LookupComponent componentsOutputLocation,
            final FieldComponent flowTypeOutComponent, final LookupComponent productsInputLocation) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        Entity technology = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER)
                .getBelongsToField(OrderFields.TECHNOLOGY);
        String range = technology.getStringField(TechnologyFieldsPFTD.RANGE);

        if (Range.ONE_DIVISION.getStringValue().equals(range)) {
            FieldComponent roleField = (FieldComponent) view.getComponentByReference(ProductionCountingQuantityFields.ROLE);

            String role = (String) roleField.getFieldValue();

            if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {

                CheckBoxComponent isDivisionInputLocation = (CheckBoxComponent) view
                        .getComponentByReference(ProductionCountingQuantityFieldsPFTD.IS_DIVISION_INPUT_LOCATION);

                flowTypeOutComponent.setFieldValue(FlowTypeOutComponent.ACCEPT_THE_PLACE.getStringValue());
                flowTypeOutComponent.requestComponentUpdateState();
                isDivisionInputLocation
                        .setFieldValue(technology.getBooleanField(TechnologyFieldsPFTD.IS_DIVISION_INPUT_LOCATION));
                isDivisionInputLocation.requestComponentUpdateState();
                productsInputLocation
                        .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                productsInputLocation.requestComponentUpdateState();
            } else if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {

                CheckBoxComponent isDivisionLocation = (CheckBoxComponent) view
                        .getComponentByReference(ProductionCountingQuantityFieldsPFTD.IS_DIVISION_LOCATION);

                CheckBoxComponent isDivisionOutputLocation = (CheckBoxComponent) view
                        .getComponentByReference(ProductionCountingQuantityFieldsPFTD.IS_DIVISION_OUTPUT_LOCATION);

                flowTypeInComponent.setFieldValue(FlowTypeInComponent.ACCEPT_THE_PLACE.getStringValue());
                flowTypeInComponent.requestComponentUpdateState();

                isDivisionLocation.setFieldValue(technology.getBooleanField(TechnologyFieldsPFTD.IS_DIVISION_LOCATION));
                isDivisionLocation.requestComponentUpdateState();

                componentsLocation.setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION).getId());
                componentsLocation.requestComponentUpdateState();

                isDivisionOutputLocation
                        .setFieldValue(technology.getBooleanField(TechnologyFieldsPFTD.IS_DIVISION_OUTPUT_LOCATION));
                isDivisionOutputLocation.requestComponentUpdateState();

                componentsOutputLocation
                        .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                componentsOutputLocation.requestComponentUpdateState();

            }
        }
    }

}
