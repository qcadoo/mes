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

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.materialFlowResources.constants.DivisionFieldsMFR;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.Range;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductionLineCriteriaModifiersPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.ProductsFlowInCriteriaModifiers;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class TechnologyDetailsHooksPFTD {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_PRODUCTS_COMPONENT = "productsFlowComponent";

    private static final String L_PRODUCTS_FLOW_INTERMIDIATE_IN = "productsFlowIntermediateIn";

    private static final String L_PRODUCTS_FLOW_INTERMIDIATE_OUT = "productsFlowIntermediateOut";

    private static final String L_PRODUCTS_FINAL = "productsFinal";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        enableTab(view);
        // range
        setFieldsRequiredOnRangeTab(view);
        showHideDivisionField(view);
        disbaleRangeIfOperationsAreFromDifferentDivisions(view);
        setCriteriaModifierParameters(view);
        // flow
        setFieldsRequiredOnFlowTab(view);
        disableOneDivisionSections(view);
        fillFieldsForOneDivisionRange(view, false);
        changeRibbonState(view);
        hideFlowTableForManyDivision(view);
        fillFlowLocationForCumulatedProductionRecording(view);
        disableTab(view);
    }

    private void fillFlowLocationForCumulatedProductionRecording(ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingFieldComponent = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        if (typeOfProductionRecordingFieldComponent != null && TypeOfProductionRecording.CUMULATED.getStringValue()
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

    public void setWorkstationsLookup(ViewDefinitionState view) {
    }

    private void hideFlowTableForManyDivision(final ViewDefinitionState view) {
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);

        String range = (String) rangeField.getFieldValue();

        enableFlowGrids(view, !Range.ONE_DIVISION.getStringValue().equals(range), true);
    }

    private void enableFlowGrids(final ViewDefinitionState view, final boolean enable, final boolean editable) {
        GridComponent gridProductsComponent = (GridComponent) view.getComponentByReference(L_PRODUCTS_COMPONENT);
        GridComponent gridProductsIntermidiateIn = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_INTERMIDIATE_IN);
        GridComponent gridProductsIntermidiateOut = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FLOW_INTERMIDIATE_OUT);
        GridComponent gridProductsFinal = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FINAL);
        gridProductsComponent.setEnabled(enable);
        gridProductsIntermidiateIn.setEnabled(enable);
        gridProductsIntermidiateOut.setEnabled(enable);
        gridProductsFinal.setEnabled(enable);
        gridProductsComponent.setEditable(editable);
        gridProductsIntermidiateIn.setEditable(editable);
        gridProductsIntermidiateOut.setEditable(editable);
        gridProductsFinal.setEditable(editable);
    }

    private void enableRangeGrids(final ViewDefinitionState view, final boolean enable, final boolean editable) {
        GridComponent rangeTechnologyOperationComponent = (GridComponent) view
                .getComponentByReference("rangeTechnologyOperationComponent");
        GridComponent workstations = (GridComponent) view.getComponentByReference("workstations");

        rangeTechnologyOperationComponent.setEnabled(true);
        workstations.setEnabled(true);

        rangeTechnologyOperationComponent.setEditable(editable);
        workstations.setEditable(editable);
    }

    private void changeRibbonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long technologyId = technologyForm.getEntityId();
        if (technologyId == null) {
            return;
        }

        Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup flow = window.getRibbon().getGroupByName("productsFlow");

        RibbonActionItem fillLocationsInComponents = flow.getItemByName("fillLocationsInComponents");

        fillLocationsInComponents.setEnabled(TechnologyState.DRAFT.getStringValue().equals(technology.getStringField(TechnologyFields.STATE)));
        fillLocationsInComponents.requestUpdate(true);
        window.requestRibbonRender();
    }

    public void fillFieldsForOneDivisionRange(final ViewDefinitionState view, boolean isDivisonChange) {
        if (!isDivisonChange) {
            return;
        }
        LookupComponent divisionLookupComponent = (LookupComponent) view.getComponentByReference(TechnologyFieldsPFTD.DIVISION);
        if (divisionLookupComponent.getEntity() == null) {
            return;
        }

        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);

        String range = (String) rangeField.getFieldValue();

        if (!Range.ONE_DIVISION.getStringValue().equals(range)) {
            return;
        }
        Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();

        Entity division = technology.getBelongsToField(TechnologyFieldsPFTD.DIVISION);

        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);

        Entity componentsLocation = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_LOCATION);
        if (componentsLocation == null) {
            componentsLocationLookup.setFieldValue(null);
        } else {
            componentsLocationLookup.setFieldValue(componentsLocation.getId());
        }
        componentsLocationLookup.requestComponentUpdateState();

        Entity componentsOutput = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_OUTPUT_LOCATION);
        if (componentsOutput == null) {
            componentsOutputLocationLookup.setFieldValue(null);
        } else {
            componentsOutputLocationLookup.setFieldValue(componentsOutput.getId());
        }
        componentsOutputLocationLookup.requestComponentUpdateState();

        Entity productsInput = division.getBelongsToField(DivisionFieldsMFR.PRODUCTS_INPUT_LOCATION);
        if (productsInput == null) {
            productsInputLocationLookup.setFieldValue(null);
        } else {
            productsInputLocationLookup.setFieldValue(productsInput.getId());
        }
        productsInputLocationLookup.requestComponentUpdateState();

        Entity productsFlow = division.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        if (productsFlow == null) {
            productsFlowLocationLookup.setFieldValue(null);
        } else {
            productsFlowLocationLookup.setFieldValue(productsFlow.getId());
        }
        productsFlowLocationLookup.requestComponentUpdateState();

        FieldComponent productionFlow = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
        productionFlow.setFieldValue(division.getStringField(TechnologyFieldsPFTD.PRODUCTION_FLOW));
        productionFlow.requestComponentUpdateState();
    }

    private void disableTab(final ViewDefinitionState view) {
        FormComponent technology = (FormComponent) view.getComponentByReference(L_FORM);
        if (technology.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getEntityId());
            if (entity == null) {
                return;
            }
            String state = entity.getStringField(TechnologyFields.STATE);

            if (!TechnologyState.DRAFT.getStringValue().equals(state)) {
                enableFlowGrids(view, false, false);
                enableRangeGrids(view, false, false);
                LookupComponent componentsLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
                LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
                LookupComponent productsInputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

                LookupComponent productsFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                componentsLocationLookup.setEnabled(false);
                componentsOutputLocationLookup.setEnabled(false);
                productsInputLocationLookup.setEnabled(false);
                productsFlowLocationLookup.setEnabled(false);

                FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);
                rangeField.setEnabled(false);

                FieldComponent productionFlowFieldComponent = (FieldComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
                productionFlowFieldComponent.setEnabled(false);
            }
        }
    }

    private void enableTab(final ViewDefinitionState view) {
        FormComponent technology = (FormComponent) view.getComponentByReference(L_FORM);
        if (technology.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getEntityId());
            if (entity == null) {
                return;
            }
            String state = entity.getStringField(TechnologyFields.STATE);

            if (TechnologyState.DRAFT.getStringValue().equals(state)) {
                enableFlowGrids(view, true, true);
                enableRangeGrids(view, true, true);
                LookupComponent componentsLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
                LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
                LookupComponent productsInputLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

                LookupComponent productsFlowLocationLookup = (LookupComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
                componentsLocationLookup.setEnabled(true);
                componentsOutputLocationLookup.setEnabled(true);
                productsInputLocationLookup.setEnabled(true);
                productsFlowLocationLookup.setEnabled(true);

                FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);
                rangeField.setEnabled(false);

                FieldComponent productionFlowFieldComponent = (FieldComponent) view
                        .getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
                productionFlowFieldComponent.setEnabled(false);
            }
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent technologForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long technologyId = technologForm.getEntityId();

        if (technologyId == null) {
            return;
        }
        Entity techEntity = technologForm.getPersistedEntityWithIncludedFormValues();
        Entity division = techEntity.getBelongsToField("division");
        LookupComponent productionLineLookupComponent = (LookupComponent) view.getComponentByReference("productionLine");
        FilterValueHolder productionLineLookupComponentHolder = productionLineLookupComponent.getFilterValue();

        if (division != null) {
            productionLineLookupComponentHolder
                    .put(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER, division.getId());
            productionLineLookupComponent.setFilterValue(productionLineLookupComponentHolder);
        } else {
            if (productionLineLookupComponentHolder.has(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER)) {
                productionLineLookupComponentHolder
                        .remove(ProductionLineCriteriaModifiersPFTD.DIVISION_PARAMETER);
                productionLineLookupComponent.setFilterValue(productionLineLookupComponentHolder);
            }
        }
        GridComponent gridProductsComponent = (GridComponent) view.getComponentByReference(L_PRODUCTS_COMPONENT);
        GridComponent gridProductsIntermidiateIn = (GridComponent) view.getComponentByReference(L_PRODUCTS_FLOW_INTERMIDIATE_IN);
        GridComponent gridProductsIntermidiateOut = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FLOW_INTERMIDIATE_OUT);
        GridComponent gridProductsFinal = (GridComponent) view
                .getComponentByReference(L_PRODUCTS_FINAL);

        FilterValueHolder gridProductsComponentInHolder = gridProductsComponent.getFilterValue();
        gridProductsComponentInHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsComponent.setFilterValue(gridProductsComponentInHolder);

        FilterValueHolder gridProductsIntermidiateInHolder = gridProductsIntermidiateIn.getFilterValue();
        gridProductsIntermidiateInHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsIntermidiateIn.setFilterValue(gridProductsIntermidiateInHolder);

        FilterValueHolder gridProductsIntermidiateOutHolder = gridProductsIntermidiateOut.getFilterValue();
        gridProductsIntermidiateOutHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsIntermidiateOut.setFilterValue(gridProductsIntermidiateOutHolder);

        FilterValueHolder gridProductsFinalOutHolder = gridProductsFinal.getFilterValue();
        gridProductsFinalOutHolder.put(ProductsFlowInCriteriaModifiers.TECHNOLOGY_PARAMETER, technologyId);
        gridProductsFinal.setFilterValue(gridProductsFinalOutHolder);
    }

    private void disableOneDivisionSections(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (technologyForm.getEntityId() == null) {
            return;
        }

        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);
        String range = (String) rangeField.getFieldValue();

        enableSection(view, Range.ONE_DIVISION.getStringValue().equals(range));
    }

    private void enableSection(final ViewDefinitionState view, final boolean b) {
        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        FieldComponent productionFlow = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_FLOW);
        componentsLocationLookup.setEnabled(b);
        componentsOutputLocationLookup.setEnabled(b);
        productsFlowLocationLookup.setEnabled(b);
        productsInputLocationLookup.setEnabled(b);
        productionFlow.setEnabled(b);

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity technology = form.getPersistedEntityWithIncludedFormValues();
        if (Range.ONE_DIVISION.getStringValue().equals(technology.getStringField(TechnologyFieldsPFTD.RANGE)) && b) {
            productsFlowLocationLookup.setEnabled(ProductionFlowComponent.WAREHOUSE.getStringValue().equals(
                    technology.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW)));
        }
    }

    public void disbaleRangeIfOperationsAreFromDifferentDivisions(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (technologyForm.getEntityId() == null) {
            return;
        }

        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);

        Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();

        List<Entity> technologyOperationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();

        Set<Entity> divisons = Sets.newHashSet();

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            List<Entity> workstations = technologyOperationComponent
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
            for (Entity workstation : workstations) {
                divisons.add(workstation.getBelongsToField(WorkstationFields.DIVISION));
            }
        }

        if (divisons.size() > 1) {
            FieldComponent divisionField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.DIVISION);
            showField(divisionField, false);
            rangeField.setFieldValue(Range.MANY_DIVISIONS.getStringValue());
            rangeField.setEnabled(false);
            rangeField.requestComponentUpdateState();
        } else {
            rangeField.setEnabled(true);
            rangeField.requestComponentUpdateState();
        }
    }

    public void showHideDivisionField(final ViewDefinitionState view) {
        FieldComponent divisionField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.DIVISION);
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.RANGE);
        FieldComponent productionLineField = (FieldComponent) view.getComponentByReference(TechnologyFieldsPFTD.PRODUCTION_LINE);

        String range = (String) rangeField.getFieldValue();

        if (Range.ONE_DIVISION.getStringValue().equals(range)) {
            showField(productionLineField, true);
            showField(divisionField, true);

        } else {
            showField(productionLineField, false);
            showField(divisionField, false);

        }
    }

    private void showField(final FieldComponent fieldComponent, final boolean isVisible) {
        fieldComponent.setVisible(isVisible);
    }

    private void setFieldsRequiredOnRangeTab(final ViewDefinitionState view) {
        List<String> references = Collections.singletonList(TechnologyFieldsPFTD.RANGE);

        setFieldsRequired(view, references);
    }

    private void setFieldsRequiredOnFlowTab(final ViewDefinitionState view) {
        List<String> references = Arrays.asList(TechnologyFieldsPFTD.COMPONENTS_LOCATION,
                TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);

        setFieldsRequired(view, references);
    }

    private void setFieldsRequired(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setRequired(true);
            fieldComponent.requestComponentUpdateState();
        }
    }

}
