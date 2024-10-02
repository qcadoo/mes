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
package com.qcadoo.mes.technologies.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.criteriaModifiers.QualityCardCriteriaModifiers;
import com.qcadoo.mes.technologies.criteriaModifiers.TechnologyDetailsCriteriaModifiers;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields.STATUS;

@Service
public class TechnologyDetailsHooks {

    private static final String L_IMPORT = "import";

    private static final String L_OPEN_OPERATION_PRODUCT_IN_COMPONENTS_IMPORT_PAGE = "openOperationProductInComponentsImportPage";

    private static final String L_FILL_PRODUCTS = "fillProducts";

    private static final String L_MOVE_PRODUCTS = "moveProducts";

    private static final String L_TREE_TAB = "treeTab";

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    private static final String L_OPERATION_COMPONENTS = "operationComponents";

    private static final String L_TECHNOLOGICAL_PROCESS_COMPONENTS = "technologicalProcessComponents";

    private static final String L_IN_PRODUCTS = "inProducts";

    private static final String L_PRODUCT_DATA_ACTIONS = "productDataActions";

    private static final String L_ON_PRODUCT_CART = "onProductCart";

    private static final String OPERATION_RANGE_DESCRIPTION = "operationRangeDescription";

    private static final String RANGE_TECHNOLOGY_OPERATION_COMPONENT = "rangeTechnologyOperationComponent";

    private static final String WORKSTATIONS_FOR_TOC_LOOKUP = "workstationsForTOClookup";

    private static final String WORKSTATIONS_TECHNOLOGY_OPERATION_COMPONENT = "workstationsTechnologyOperationComponent";

    private static final String WORKSTATIONS = "workstations";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setTechnologyIdForMultiUploadField(view);
        disableFieldTechnologyFormAndEnabledMaster(view);
        filterStateChangeHistory(view);
        setTreeTabEditable(view);
        setRibbonState(view);
        setProductDataRibbonState(view);
        fillCriteriaModifiers(view);
        enableTab(view);
        fillRangeAndDivision(view);
        setFieldsRequiredOnRangeTab(view);
        showHideDivisionField(view);
        setCriteriaModifierParameters(view);
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
                enableRangeGrids(view, true);
            }
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
                enableRangeGrids(view, false);
                FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);
                rangeField.setEnabled(false);
            }
        }
    }

    private void enableRangeGrids(final ViewDefinitionState view, final boolean editable) {
        GridComponent rangeTechnologyOperationComponent = (GridComponent) view
                .getComponentByReference(RANGE_TECHNOLOGY_OPERATION_COMPONENT);
        GridComponent productionLines = (GridComponent) view
                .getComponentByReference(TechnologyFields.PRODUCTION_LINES);
        GridComponent workstationsTechnologyOperationComponent = (GridComponent) view
                .getComponentByReference(WORKSTATIONS_TECHNOLOGY_OPERATION_COMPONENT);
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(WORKSTATIONS);

        productionLines.setEnabled(editable);
        workstations.setEnabled(editable && !workstationsTechnologyOperationComponent.getSelectedEntities().isEmpty());
        rangeTechnologyOperationComponent.setEnabled(true);

        rangeTechnologyOperationComponent.setEditable(editable);
    }


    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.isNull(technologyId)) {
            return;
        }

        GridComponent rangeTechnologyOperationComponent = (GridComponent) view.getComponentByReference(RANGE_TECHNOLOGY_OPERATION_COMPONENT);
        FilterValueHolder rangeTechnologyOperationComponentFilterValueHolder = rangeTechnologyOperationComponent.getFilterValue();

        rangeTechnologyOperationComponentFilterValueHolder.put(TechnologyDetailsCriteriaModifiers.L_TECHNOLOGY_ID,
                technologyId);

        rangeTechnologyOperationComponent.setFilterValue(rangeTechnologyOperationComponentFilterValueHolder);

        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);
        String range = (String) rangeField.getFieldValue();
        Entity division = null;

        if (Range.ONE_DIVISION.getStringValue().equals(range)) {
            Entity techEntity = technologyForm.getPersistedEntityWithIncludedFormValues();
            division = techEntity.getBelongsToField(TechnologyFields.DIVISION);
        } else {
            GridComponent workstationsTechnologyOperationComponent = (GridComponent) view
                    .getComponentByReference(WORKSTATIONS_TECHNOLOGY_OPERATION_COMPONENT);
            Set<Long> selectedEntitiesIds = workstationsTechnologyOperationComponent.getSelectedEntitiesIds();
            if (!selectedEntitiesIds.isEmpty()) {
                Entity toc = getTechnologyOperationComponentDD().get(selectedEntitiesIds.stream().findFirst().get());
                division = toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
            }
        }

        LookupComponent workstationsForTOCLookupComponent = (LookupComponent) view
                .getComponentByReference(WORKSTATIONS_FOR_TOC_LOOKUP);

        FilterValueHolder workstationsForTOCLookupComponentHolder = workstationsForTOCLookupComponent.getFilterValue();

        if (Objects.nonNull(division)) {
            workstationsForTOCLookupComponentHolder.put(WorkstationFields.DIVISION, division.getId());
        } else if (workstationsForTOCLookupComponentHolder.has(WorkstationFields.DIVISION)) {
            workstationsForTOCLookupComponentHolder.remove(WorkstationFields.DIVISION);
        }

        workstationsForTOCLookupComponent.setFilterValue(workstationsForTOCLookupComponentHolder);
    }


    private void fillRangeAndDivision(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(TechnologyFields.DIVISION);

        Long technologyId = technologyForm.getEntityId();

        if (Objects.isNull(technologyId) && view.isViewAfterRedirect()) {
            String range = parameterService.getParameter().getStringField(ParameterFieldsT.RANGE);
            Entity division = parameterService.getParameter().getBelongsToField(ParameterFieldsT.DIVISION);

            rangeField.setFieldValue(range);

            if (Objects.nonNull(division)) {
                divisionLookup.setFieldValue(division.getId());
            } else {
                divisionLookup.setFieldValue(null);
            }
        }

        rangeField.requestComponentUpdateState();
        divisionLookup.requestComponentUpdateState();
    }

    private void setFieldsRequiredOnRangeTab(final ViewDefinitionState view) {
        List<String> references = Collections.singletonList(TechnologyFields.RANGE);

        setFieldsRequired(view, references);
    }

    private void setFieldsRequired(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setRequired(true);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private void showHideDivisionField(final ViewDefinitionState view) {
        FieldComponent rangeField = (FieldComponent) view.getComponentByReference(TechnologyFields.RANGE);
        FieldComponent divisionField = (FieldComponent) view.getComponentByReference(TechnologyFields.DIVISION);

        String range = (String) rangeField.getFieldValue();
        GridComponent rangeTechnologyOperationComponent = (GridComponent) view
                .getComponentByReference(RANGE_TECHNOLOGY_OPERATION_COMPONENT);
        ComponentState operationRangeDescriptionLabel = view.getComponentByReference(OPERATION_RANGE_DESCRIPTION);
        if (Range.ONE_DIVISION.getStringValue().equals(range)) {
            showField(divisionField, true);
            rangeTechnologyOperationComponent.setVisible(false);
            operationRangeDescriptionLabel.setVisible(false);
        } else {
            showField(divisionField, false);
            rangeTechnologyOperationComponent.setVisible(true);
            operationRangeDescriptionLabel.setVisible(true);
        }
    }

    private void showField(final FieldComponent fieldComponent, final boolean isVisible) {
        fieldComponent.setVisible(isVisible);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void setTreeTabEditable(final ViewDefinitionState view) {
        final boolean treeTabShouldBeEnabled = !isTemplateAccepted(view) && TechnologyState.DRAFT.equals(getTechnologyState(view))
                && technologyIsAlreadySaved(view);

        ComponentState technologyTree = view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        technologyTree.setEnabled(treeTabShouldBeEnabled);

        Long selectedEntity = ((TreeComponent) technologyTree).getSelectedEntityId();

        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            GridComponent grid = (GridComponent) view.getComponentByReference(componentReference);
            grid.setEnabled(treeTabShouldBeEnabled && Objects.nonNull(selectedEntity));
        }
    }

    private boolean isTemplateAccepted(final ViewDefinitionState view) {
        CheckBoxComponent isTemplateAcceptedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(TechnologyFields.IS_TEMPLATE_ACCEPTED);

        if (Objects.nonNull(isTemplateAcceptedCheckBox)) {
            return isTemplateAcceptedCheckBox.isChecked();
        }

        return false;
    }

    public void setTreeTabEditable(final ViewDefinitionState view, final boolean treeTabShouldBeEnabled) {
        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            ((GridComponent) view.getComponentByReference(componentReference)).setEditable(treeTabShouldBeEnabled);
        }

        view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE).setEnabled(treeTabShouldBeEnabled);
    }

    private boolean technologyIsAlreadySaved(final ViewDefinitionState view) {
        final FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        return Objects.nonNull(technologyForm.getEntityId());
    }

    private TechnologyState getTechnologyState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        final Entity technology = form.getEntity();

        TechnologyState state = TechnologyState.DRAFT;

        if (Objects.nonNull(technology)) {
            state = TechnologyState.parseString(technology.getStringField(TechnologyFields.STATE));
        }

        return state;
    }

    public void disableFieldTechnologyFormAndEnabledMaster(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent masterField = (FieldComponent) view.getComponentByReference(TechnologyFields.MASTER);
        LookupComponent technologyGroupLookup = (LookupComponent) view.getComponentByReference(TechnologyFields.TECHNOLOGY_GROUP);

        boolean isEnabled = true;
        boolean isMasterEnabled = false;
        boolean isTechnologyGroupEnabled = false;

        Long technologyId = technologyForm.getEntityId();

        if (Objects.nonNull(technologyId)) {
            Entity technology = getTechnologyDD().get(technologyId);

            if (Objects.isNull(technology)) {
                return;
            }

            String state = technology.getStringField(TechnologyFields.STATE);
            boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);
            boolean master = technology.getBooleanField(TechnologyFields.MASTER);

            if (isTemplateAccepted || !TechnologyState.DRAFT.getStringValue().equals(state)) {
                isEnabled = false;
            }
            if (TechnologyState.ACCEPTED.getStringValue().equals(state) && !master) {
                isMasterEnabled = true;
            }
            if (TechnologyState.ACCEPTED.getStringValue().equals(state) || TechnologyState.CHECKED.getStringValue().equals(state)
                    || TechnologyState.DRAFT.getStringValue().equals(state)) {
                isTechnologyGroupEnabled = true;
            }
        }

        technologyForm.setFormEnabled(isEnabled);

        masterField.setEnabled(isMasterEnabled);
        masterField.requestComponentUpdateState();
        technologyGroupLookup.setEnabled(isTechnologyGroupEnabled);
        technologyGroupLookup.requestComponentUpdateState();
    }

    // TODO hotfix for issue-1901 with restoring previous active tab state after back operation, requires fixes in framework
    public void navigateToActiveTab(final ViewDefinitionState view) {
        TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        Long selectedEntity = technologyTree.getSelectedEntityId();

        if (Objects.nonNull(selectedEntity)) {
            ((WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW)).setActiveTab(L_TREE_TAB);
        }
    }

    public void setTechnologyIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent technologyIdForMultiUpload = (FieldComponent) view.getComponentByReference("technologyIdForMultiUpload");
        FieldComponent technologyMultiUploadLocale = (FieldComponent) view.getComponentByReference("technologyMultiUploadLocale");

        Long technologyId = technologyForm.getEntityId();

        if (Objects.nonNull(technologyId)) {
            technologyIdForMultiUpload.setFieldValue(technologyId);
        } else {
            technologyIdForMultiUpload.setFieldValue("");
        }

        technologyIdForMultiUpload.requestComponentUpdateState();

        technologyMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        technologyMultiUploadLocale.requestComponentUpdateState();
    }

    private void setRibbonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup fillProductsGroup = window.getRibbon().getGroupByName(L_FILL_PRODUCTS);
        RibbonActionItem fillProductsRibbonActionItem = fillProductsGroup.getItemByName(L_FILL_PRODUCTS);
        RibbonActionItem moveProductsRibbonActionItem = fillProductsGroup.getItemByName(L_MOVE_PRODUCTS);
        RibbonActionItem openOperationProductInComponentsImportPageRibbonActionItem = window.getRibbon().getGroupByName(L_IMPORT)
                .getItemByName(L_OPEN_OPERATION_PRODUCT_IN_COMPONENTS_IMPORT_PAGE);

        Entity technology = technologyForm.getEntity();

        String state = technology.getStringField(TechnologyFields.STATE);

        boolean isSaved = Objects.nonNull(technologyForm.getEntityId());
        boolean isDraft = TechnologyState.DRAFT.getStringValue().equals(state);
        boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);

        fillProductsRibbonActionItem.setEnabled(isSaved && isDraft && !isTemplateAccepted);
        fillProductsRibbonActionItem
                .setMessage("technologies.technologyDetails.window.ribbon.fillProducts.fillProducts.description");
        fillProductsRibbonActionItem.requestUpdate(true);
        moveProductsRibbonActionItem.setEnabled(isSaved && isDraft && !isTemplateAccepted);
        moveProductsRibbonActionItem
                .setMessage("technologies.technologyDetails.window.ribbon.fillProducts.moveProducts.description");
        moveProductsRibbonActionItem.requestUpdate(true);
        openOperationProductInComponentsImportPageRibbonActionItem.setEnabled(isSaved && isDraft && !isTemplateAccepted);
        openOperationProductInComponentsImportPageRibbonActionItem.setMessage(
                "technologies.technologyDetails.window.ribbon.import.openOperationProductInComponentsImportPage.description");
        openOperationProductInComponentsImportPageRibbonActionItem.requestUpdate(true);
    }

    public void setProductDataRibbonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent inProductsGrid = (GridComponent) view.getComponentByReference(L_IN_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup productDataActionsGroup = window.getRibbon().getGroupByName(L_PRODUCT_DATA_ACTIONS);
        RibbonActionItem onProductCartRibbonActionItem = productDataActionsGroup.getItemByName(L_ON_PRODUCT_CART);

        Entity technology = technologyForm.getEntity();

        String state = technology.getStringField(TechnologyFields.STATE);

        boolean isSaved = Objects.nonNull(technologyForm.getEntityId());
        boolean isDraft = TechnologyState.DRAFT.getStringValue().equals(state);

        onProductCartRibbonActionItem.setEnabled(isSaved && isDraft && !inProductsGrid.getSelectedEntities().isEmpty());
        onProductCartRibbonActionItem.requestUpdate(true);
    }

    private void fillCriteriaModifiers(final ViewDefinitionState view) {
        LookupComponent product = (LookupComponent) view.getComponentByReference(TechnologyFields.PRODUCT);
        LookupComponent qualityCard = (LookupComponent) view.getComponentByReference(TechnologyFields.QUALITY_CARD);

        if (Objects.nonNull(product.getEntity())) {
            FilterValueHolder filter = qualityCard.getFilterValue();

            filter.put(QualityCardCriteriaModifiers.L_PRODUCT_ID, product.getEntity().getId());

            qualityCard.setFilterValue(filter);
            qualityCard.requestComponentUpdateState();
        }

        GridComponent operationComponents = (GridComponent) view.getComponentByReference(L_OPERATION_COMPONENTS);
        FilterValueHolder operationComponentsFilterValueHolder = operationComponents.getFilterValue();

        operationComponentsFilterValueHolder.put(TechnologyDetailsCriteriaModifiers.L_TECHNOLOGY_ID,
                ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntityId());

        operationComponents.setFilterValue(operationComponentsFilterValueHolder);

        GridComponent technologicalProcessComponents = (GridComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_COMPONENTS);
        FilterValueHolder gridFilterValueHolder = technologicalProcessComponents.getFilterValue();

        Optional<Long> selectedEntityId = operationComponents.getSelectedEntitiesIds().stream().findFirst();

        if (selectedEntityId.isPresent()) {
            gridFilterValueHolder.put(TechnologyDetailsCriteriaModifiers.L_TECHNOLOGY_OPERATION_COMPONENT_ID,
                    selectedEntityId.get());
        } else {
            gridFilterValueHolder.remove(TechnologyDetailsCriteriaModifiers.L_TECHNOLOGY_OPERATION_COMPONENT_ID);
        }

        technologicalProcessComponents.setFilterValue(gridFilterValueHolder);
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }
}
