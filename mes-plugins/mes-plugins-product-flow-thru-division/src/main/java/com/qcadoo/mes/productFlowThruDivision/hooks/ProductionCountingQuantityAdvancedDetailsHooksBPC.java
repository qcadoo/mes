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
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Objects;

@Service
public class ProductionCountingQuantityAdvancedDetailsHooksBPC {

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
        LookupComponent tocComponent = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent select = (FieldComponent) view
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
        select.setEnabled(false);
        select.requestComponentUpdateState();
        productsFlowLocationLookup.setEnabled(false);
        productsFlowLocationLookup.requestComponentUpdateState();

        wasteReceptionWarehouseLookup.setEnabled(false);
        wasteReceptionWarehouseLookup.requestComponentUpdateState();

        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type) || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))) {
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
            select.setEnabled(true);
            select.requestComponentUpdateState();
        }

        Entity ent = form.getPersistedEntityWithIncludedFormValues();
        Entity order = ent.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        if (ent.getId() == null) {
            componentsLocationLookup.setFieldValue(null);
            componentsLocationLookup.requestComponentUpdateState();
            componentsOutputLocationLookup.setFieldValue(null);
            componentsOutputLocationLookup.requestComponentUpdateState();
            productsInputLocationLookup.setFieldValue(null);
            productsInputLocationLookup.requestComponentUpdateState();
            wasteReceptionWarehouseLookup.setFieldValue(null);
            wasteReceptionWarehouseLookup.requestComponentUpdateState();
            if (!ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                select.setFieldValue(null);
                select.requestComponentUpdateState();
            }
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            String range = technology.getStringField(TechnologyFieldsPFTD.RANGE);

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

                if ("".equals(select.getFieldValue())
                        && ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                    select.setFieldValue(technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
                    select.requestComponentUpdateState();
                }

                if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)
                        && ProductionFlowComponent.WAREHOUSE.getStringValue().equals(select.getFieldValue())
                        && technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {
                    productsFlowLocationLookup
                            .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                    productsFlowLocationLookup.requestComponentUpdateState();
                }

                if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                        && (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type) || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))) {
                    productsInputLocationLookup
                            .setFieldValue(technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                    productsInputLocationLookup.requestComponentUpdateState();
                }

                if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                        && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)) {
                    if (Objects.nonNull(technology.getBelongsToField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE))) {
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

                        if ("".equals(select.getFieldValue())
                                && ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                            select.setFieldValue(division.getField(DivisionFieldsPFTD.PRODUCTION_FLOW));
                            select.requestComponentUpdateState();
                        }

                        if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)
                                && ProductionFlowComponent.WAREHOUSE.getStringValue().equals(select.getFieldValue())
                                && division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION) != null) {
                            productsFlowLocationLookup
                                    .setFieldValue(division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_FLOW_LOCATION).getId());
                            productsFlowLocationLookup.requestComponentUpdateState();
                        }
                        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                                && (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type) || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type))
                                && division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION) != null) {
                            productsInputLocationLookup.setFieldValue(
                                    division.getBelongsToField(DivisionFieldsPFTD.PRODUCTS_INPUT_LOCATION).getId());
                            productsInputLocationLookup.requestComponentUpdateState();
                        }
                        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                                && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)
                                && division.getBelongsToField(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE) != null) {
                            wasteReceptionWarehouseLookup.setFieldValue(
                                    division.getBelongsToField(DivisionFieldsPFTD.WASTE_RECEPTION_WAREHOUSE).getId());
                            wasteReceptionWarehouseLookup.requestComponentUpdateState();
                        }
                    }
                }
            }
        } else if (!ProductionFlowComponent.WAREHOUSE.getStringValue().equals(select.getFieldValue())) {
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();
        }

        if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)
                && TypeOfProductionRecording.CUMULATED.getStringValue()
                .equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            select.setFieldValue(ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
            select.setEnabled(false);
            select.requestComponentUpdateState();
            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.requestComponentUpdateState();
        }

        productsFlowLocationLookup.setEnabled(ProductionFlowComponent.WAREHOUSE.getStringValue().equals(select.getFieldValue()));
    }
}
