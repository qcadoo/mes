/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.listeners;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.mes.technologies.constants.Range;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

@Service
public class ProductionCountingQuantityAdvancedDetailsListenersPFTD {

    public void setFlowTabState(final ViewDefinitionState view, final ComponentState componentState,
                                final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent tocComponent = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
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

        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        Entity order = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        if (ent.getId() == null) {
            String role = (String) roleField.getFieldValue();
            String type = (String) typeField.getFieldValue();

            componentsLocationLookup.setFieldValue(null);
            componentsLocationLookup.requestComponentUpdateState();
            componentsOutputLocationLookup.setFieldValue(null);
            componentsOutputLocationLookup.requestComponentUpdateState();
            productsInputLocationLookup.setFieldValue(null);
            productsInputLocationLookup.requestComponentUpdateState();
            wasteReceptionWarehouseLookup.setFieldValue(null);
            wasteReceptionWarehouseLookup.requestComponentUpdateState();
            productionFlowSelect.setFieldValue(null);
            productionFlowSelect.requestComponentUpdateState();
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            String range = technology.getStringField(TechnologyFields.RANGE);

            if (Range.ONE_DIVISION.getStringValue().equals(range)) {
                if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                        && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                    if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION) != null) {
                        componentsLocationLookup
                                .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION).getId());
                        componentsLocationLookup.requestComponentUpdateState();
                    }
                    if (technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION) != null) {
                        componentsOutputLocationLookup.setFieldValue(
                                technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                        componentsOutputLocationLookup.requestComponentUpdateState();
                    }
                }

                if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                    productionFlowSelect.setFieldValue(technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
                    productionFlowSelect.requestComponentUpdateState();

                    if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlowSelect.getFieldValue())
                            && technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {
                        productsFlowLocationLookup
                                .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                        productsFlowLocationLookup.requestComponentUpdateState();
                    }
                }

                if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                    if ((ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)
                            || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))
                            && technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION) != null) {
                        productsInputLocationLookup
                                .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                        productsInputLocationLookup.requestComponentUpdateState();
                    }

                    if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)
                            && technology.getBelongsToField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE) != null) {
                        wasteReceptionWarehouseLookup.setFieldValue(
                                technology.getBelongsToField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE).getId());
                        wasteReceptionWarehouseLookup.requestComponentUpdateState();
                    }
                }
            } else if (Range.MANY_DIVISIONS.getStringValue().equals(range)) {
                Entity toc = tocComponent.getEntity();
                if (toc != null) {
                    Entity division = toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
                    if (division != null) {
                        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                            if (division.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION) != null) {
                                componentsLocationLookup.setFieldValue(
                                        division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_LOCATION).getId());
                                componentsLocationLookup.requestComponentUpdateState();
                            }
                            if (division.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION) != null) {
                                componentsOutputLocationLookup.setFieldValue(
                                        division.getBelongsToField(DivisionFieldsPFTD.COMPONENTS_OUTPUT_LOCATION).getId());
                                componentsOutputLocationLookup.requestComponentUpdateState();
                            }
                        }

                        if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                            productionFlowSelect.setFieldValue(division.getField(DivisionFieldsPFTD.PRODUCTION_FLOW));
                            productionFlowSelect.requestComponentUpdateState();

                            if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlowSelect.getFieldValue())
                                    && division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {
                                productsFlowLocationLookup
                                        .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                                productsFlowLocationLookup.requestComponentUpdateState();
                            }
                        }
                        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                            if ((ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)
                                    || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))
                                    && division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION) != null) {
                                productsInputLocationLookup.setFieldValue(
                                        division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                                productsInputLocationLookup.requestComponentUpdateState();
                            }
                            if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)
                                    && division.getBelongsToField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE) != null) {
                                wasteReceptionWarehouseLookup.setFieldValue(
                                        division.getBelongsToField(DivisionFieldsPFTD.WASTE_RECEPTION_WAREHOUSE).getId());
                                wasteReceptionWarehouseLookup.requestComponentUpdateState();
                            }
                        }
                    }
                }
            }
        } else if (!ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlowSelect.getFieldValue())) {
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();
        }
    }
}