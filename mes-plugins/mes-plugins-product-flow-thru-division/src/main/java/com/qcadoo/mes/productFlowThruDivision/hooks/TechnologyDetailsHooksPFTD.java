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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DivisionFieldsMFR;
import com.qcadoo.mes.productFlowThruDivision.constants.DivisionFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductsFlowInCriteriaModifiers;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.mes.technologies.constants.Range;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class TechnologyDetailsHooksPFTD {

    private static final String L_PRODUCTS_COMPONENT = "productsFlowComponent";

    private static final String L_PRODUCTS_FLOW_INTERMEDIATE_IN = "productsFlowIntermediateIn";

    private static final String L_PRODUCTS_FLOW_INTERMEDIATE_OUT = "productsFlowIntermediateOut";

    private static final String L_PRODUCTS_FINAL = "productsFinal";

    private static final String L_PRODUCTS_FLOW = "productsFlow";

    private static final String L_FILL_LOCATIONS_IN_COMPONENTS = "fillLocationsInComponents";
    public static final String L_PRODUCTS_FLOW_WASTE_RECEPTION_WAREHOUSE = "productsFlowWasteReceptionWarehouse";

    public static final String MODEL_CARD = "modelCard";
    public static final String CREATE_MODEL_CARD = "createModelCard";
    public static final String PRODUCTION_LINES = "productionLines";
    public static final String ADD_MULTIPLE_PRODUCTION_LINES = "addMultipleProductionLines";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        enableTab(view);
        // range
        fillFieldsForOneDivisionRange(view);
        setCriteriaModifierParameters(view);
        // flow
        setFieldsRequiredOnFlowTab(view);
        disableOneDivisionSections(view);
        changeRibbonState(view);
        hideFlowTableForManyDivision(view);
        fillFlowLocationForCumulatedProductionRecording(view);
        disableTab(view);
    }

    private void enableTab(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.nonNull(technologyId)) {
            Entity technology = getTechnologyDD().get(technologyId);

            if (Objects.isNull(technology)) {
                return;
            }

            String state = technology.getStringField(TechnologyFields.STATE);
            boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);

            if (!isTemplateAccepted && TechnologyState.DRAFT.getStringValue().equals(state)) {
                enableFlowGrids(view, true, true);

                LookupComponent componentsLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
                LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
                LookupComponent productsInputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

                LookupComponent productsFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                LookupComponent wasteFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);

                componentsLocationLookup.setEnabled(true);
                componentsOutputLocationLookup.setEnabled(true);
                productsInputLocationLookup.setEnabled(true);
                productsFlowLocationLookup.setEnabled(true);
                wasteFlowLocationLookup.setEnabled(true);

                FieldComponent productionFlowFieldComponent = (FieldComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
                productionFlowFieldComponent.setEnabled(false);
            }
        }
    }

    private void enableFlowGrids(final ViewDefinitionState view, final boolean isEnabled, final boolean isEditable) {
        GridComponent gridProductsComponent = (GridComponent) view.getComponentByReference(L_PRODUCTS_COMPONENT);
        GridComponent gridProductsIntermediateIn = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_INTERMEDIATE_IN);
        GridComponent gridProductsIntermediateOut = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FLOW_INTERMEDIATE_OUT);
        GridComponent gridProductsFinal = (GridComponent) view.getComponentByReference(L_PRODUCTS_FINAL);
        GridComponent gridProductsWaste = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_WASTE_RECEPTION_WAREHOUSE);

        gridProductsComponent.setEnabled(isEnabled);
        gridProductsIntermediateIn.setEnabled(isEnabled);
        gridProductsIntermediateOut.setEnabled(isEnabled);
        gridProductsFinal.setEnabled(isEnabled);
        gridProductsWaste.setEnabled(isEnabled);
        gridProductsComponent.setEditable(isEditable);
        gridProductsIntermediateIn.setEditable(isEditable);
        gridProductsIntermediateOut.setEditable(isEditable);
        gridProductsFinal.setEditable(isEditable);
        gridProductsWaste.setEditable(isEditable);
    }

    private void setFieldsRequired(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setRequired(true);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.isNull(technologyId)) {
            return;
        }

        GridComponent gridProductsComponent = (GridComponent) view.getComponentByReference(L_PRODUCTS_COMPONENT);
        GridComponent gridProductsIntermediateIn = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_INTERMEDIATE_IN);
        GridComponent gridProductsIntermediateOut = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FLOW_INTERMEDIATE_OUT);
        GridComponent gridProductsFinal = (GridComponent) view.getComponentByReference(L_PRODUCTS_FINAL);
        GridComponent gridProductsWaste = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_WASTE_RECEPTION_WAREHOUSE);

        FilterValueHolder gridProductsComponentInHolder = gridProductsComponent.getFilterValue();
        gridProductsComponentInHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsComponent.setFilterValue(gridProductsComponentInHolder);

        FilterValueHolder gridProductsIntermediateInHolder = gridProductsIntermediateIn.getFilterValue();
        gridProductsIntermediateInHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsIntermediateIn.setFilterValue(gridProductsIntermediateInHolder);

        FilterValueHolder gridProductsIntermediateOutHolder = gridProductsIntermediateOut.getFilterValue();
        gridProductsIntermediateOutHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsIntermediateOut.setFilterValue(gridProductsIntermediateOutHolder);

        FilterValueHolder gridProductsFinalOutHolder = gridProductsFinal.getFilterValue();
        gridProductsFinalOutHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsFinal.setFilterValue(gridProductsFinalOutHolder);

        FilterValueHolder gridProductsWasteOutHolder = gridProductsWaste.getFilterValue();
        gridProductsWasteOutHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsWaste.setFilterValue(gridProductsWasteOutHolder);
    }

    private void setFieldsRequiredOnFlowTab(final ViewDefinitionState view) {
        List<String> references = Arrays.asList(TechnologyFieldsPFTD.COMPONENTS_LOCATION,
                TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

        setFieldsRequired(view, references);
    }

    private void disableOneDivisionSections(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.isNull(technologyId)) {
            return;
        }

        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);
        String range = (String) rangeField.getFieldValue();

        enableSection(view, Range.ONE_DIVISION.getStringValue().equals(range));
    }

    private void enableSection(final ViewDefinitionState view, final boolean isEnabled) {
        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        LookupComponent productsWasteLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);
        FieldComponent productionFlow = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);

        componentsLocationLookup.setEnabled(isEnabled);
        componentsOutputLocationLookup.setEnabled(isEnabled);
        productsFlowLocationLookup.setEnabled(isEnabled);
        productsInputLocationLookup.setEnabled(isEnabled);
        productsWasteLocationLookup.setEnabled(isEnabled);
        productionFlow.setEnabled(isEnabled);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity technology = form.getPersistedEntityWithIncludedFormValues();

        if (Range.ONE_DIVISION.getStringValue().equals(technology.getStringField(TechnologyFields.RANGE)) && isEnabled) {
            productsFlowLocationLookup.setEnabled(ProductionFlowComponent.WAREHOUSE.getStringValue()
                    .equals(technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW)));
        }
    }

    private void fillFieldsForOneDivisionRange(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long technologyId = technologyForm.getEntityId();
        if (Objects.isNull(technologyId) && view.isViewAfterRedirect()) {
            Entity division = parameterService.getParameter().getBelongsToField(ParameterFieldsT.DIVISION);
            if (Objects.nonNull(division)) {
                LookupComponent divisionLookupComponent = (LookupComponent) view.getComponentByReference(TechnologyFields.DIVISION);

                if (Objects.isNull(divisionLookupComponent.getEntity())) {
                    return;
                }

                FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);

                String range = (String) rangeField.getFieldValue();

                if (!Range.ONE_DIVISION.getStringValue().equals(range)) {
                    return;
                }

                Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();

                division = technology.getBelongsToField(TechnologyFields.DIVISION);

                LookupComponent componentsLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
                LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
                LookupComponent productsInputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);
                LookupComponent productsFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                LookupComponent productsWasteLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);

                Entity componentsLocation = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_LOCATION);

                if (Objects.isNull(componentsLocation)) {
                    componentsLocationLookup.setFieldValue(null);
                } else {
                    componentsLocationLookup.setFieldValue(componentsLocation.getId());
                }

                componentsLocationLookup.requestComponentUpdateState();

                Entity componentsOutput = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_OUTPUT_LOCATION);

                if (Objects.isNull(componentsOutput)) {
                    componentsOutputLocationLookup.setFieldValue(null);
                } else {
                    componentsOutputLocationLookup.setFieldValue(componentsOutput.getId());
                }

                componentsOutputLocationLookup.requestComponentUpdateState();

                Entity productsInput = division.getBelongsToField(DivisionFieldsMFR.PRODUCTS_INPUT_LOCATION);

                if (Objects.isNull(productsInput)) {
                    productsInputLocationLookup.setFieldValue(null);
                } else {
                    productsInputLocationLookup.setFieldValue(productsInput.getId());
                }

                productsInputLocationLookup.requestComponentUpdateState();

                Entity productsWaste = division.getBelongsToField(DivisionFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);

                if (Objects.isNull(productsWaste)) {
                    productsWasteLocationLookup.setFieldValue(null);
                } else {
                    productsWasteLocationLookup.setFieldValue(productsWaste.getId());
                }

                productsWasteLocationLookup.requestComponentUpdateState();

                Entity productsFlow = division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);

                if (Objects.isNull(productsFlow)) {
                    productsFlowLocationLookup.setFieldValue(null);
                } else {
                    productsFlowLocationLookup.setFieldValue(productsFlow.getId());
                }

                productsFlowLocationLookup.requestComponentUpdateState();

                FieldComponent productionFlow = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
                productionFlow.setFieldValue(division.getStringField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
                productionFlow.requestComponentUpdateState();
            }
        }
    }

    private void changeRibbonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.isNull(technologyId)) {
            return;
        }

        Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup flow = window.getRibbon().getGroupByName(L_PRODUCTS_FLOW);

        RibbonActionItem fillLocationsInComponents = flow.getItemByName(L_FILL_LOCATIONS_IN_COMPONENTS);

        RibbonGroup modelCard = window.getRibbon().getGroupByName(MODEL_CARD);

        RibbonGroup productionLines = window.getRibbon().getGroupByName(PRODUCTION_LINES);

        RibbonActionItem addMultipleProductionLines = productionLines.getItemByName(ADD_MULTIPLE_PRODUCTION_LINES);

        String state = technology.getStringField(TechnologyFields.STATE);
        boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);

        fillLocationsInComponents.setEnabled(!isTemplateAccepted && TechnologyState.DRAFT.getStringValue().equals(state));
        fillLocationsInComponents.requestUpdate(true);

        addMultipleProductionLines.setEnabled(!isTemplateAccepted && TechnologyState.DRAFT.getStringValue().equals(state));
        addMultipleProductionLines.requestUpdate(true);

        if (Objects.nonNull(modelCard)) {
            RibbonActionItem createModelCard = modelCard.getItemByName(CREATE_MODEL_CARD);
            createModelCard.setEnabled(TechnologyState.CHECKED.getStringValue().equals(state)
                    || TechnologyState.ACCEPTED.getStringValue().equals(state));
            createModelCard.requestUpdate(true);
        }

        window.requestRibbonRender();
    }

    private void hideFlowTableForManyDivision(final ViewDefinitionState view) {
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);

        String range = (String) rangeField.getFieldValue();

        enableFlowGrids(view, !Range.ONE_DIVISION.getStringValue().equals(range), true);
    }

    private void fillFlowLocationForCumulatedProductionRecording(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingFieldComponent = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (Objects.nonNull(typeOfProductionRecordingFieldComponent) && TypeOfProductionRecording.CUMULATED.getStringValue()
                .equals(typeOfProductionRecordingFieldComponent.getFieldValue())) {
            FieldComponent productionFlowFieldComponent = (FieldComponent) view
                    .getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);

            productionFlowFieldComponent.setFieldValue(ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
            productionFlowFieldComponent.setEnabled(false);
            productionFlowFieldComponent.requestComponentUpdateState();

            LookupComponent productsFlowLocationLookup = (LookupComponent) view
                    .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);

            productsFlowLocationLookup.setFieldValue(null);
            productsFlowLocationLookup.setEnabled(false);
            productsFlowLocationLookup.requestComponentUpdateState();
        }
    }

    private void disableTab(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.nonNull(technologyId)) {
            Entity technology = getTechnologyDD().get(technologyId);

            if (Objects.isNull(technology)) {
                return;
            }

            String state = technology.getStringField(TechnologyFields.STATE);
            boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);

            if (isTemplateAccepted || !TechnologyState.DRAFT.getStringValue().equals(state)) {
                enableFlowGrids(view, false, false);

                LookupComponent componentsLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
                LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
                LookupComponent productsInputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);
                LookupComponent productsFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                LookupComponent wasteReceptionWarehouseLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);

                componentsLocationLookup.setEnabled(false);
                componentsOutputLocationLookup.setEnabled(false);
                productsInputLocationLookup.setEnabled(false);
                productsFlowLocationLookup.setEnabled(false);
                wasteReceptionWarehouseLookup.setEnabled(false);

                FieldComponent productionFlowFieldComponent = (FieldComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
                productionFlowFieldComponent.setEnabled(false);
            }
        }
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }
}
