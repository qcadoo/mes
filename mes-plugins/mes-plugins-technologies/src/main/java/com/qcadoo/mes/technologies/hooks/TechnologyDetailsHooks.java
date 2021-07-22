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

import static com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields.STATUS;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.criteriaModifiers.QualityCardCriteriaModifiers;
import com.qcadoo.mes.technologies.criteriaModifiers.TechnologyDetailsCriteriaModifiers;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setTechnologyIdForMultiUploadField(view);
        disableFieldTechnologyFormAndEnabledMaster(view);
        filterStateChangeHistory(view);
        setTreeTabEditable(view);
        setRibbonState(view);
        fillCriteriaModifiers(view);
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

        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            GridComponent grid = (GridComponent) view.getComponentByReference(componentReference);
            grid.setEnabled(treeTabShouldBeEnabled);
        }

        view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE).setEnabled(treeTabShouldBeEnabled);
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

        boolean disabled = false;
        boolean masterDisabled = false;
        boolean technologyGroupEnabled = false;

        Long technologyId = technologyForm.getEntityId();

        if (Objects.nonNull(technologyId)) {
            Entity technology = getTechnologyDD().get(technologyId);

            if (Objects.isNull(technology)) {
                return;
            }

            String state = technology.getStringField(TechnologyFields.STATE);
            boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);

            if (isTemplateAccepted || !TechnologyState.DRAFT.getStringValue().equals(state)) {
                disabled = true;
            }
            if (TechnologyState.ACCEPTED.getStringValue().equals(state)) {
                masterDisabled = true;
            }
            if (TechnologyState.ACCEPTED.getStringValue().equals(state) || TechnologyState.CHECKED.getStringValue().equals(state)
                    || TechnologyState.DRAFT.getStringValue().equals(state)) {
                technologyGroupEnabled = true;
            }
        }

        technologyForm.setFormEnabled(!disabled);
        masterField.setEnabled(masterDisabled);
        masterField.requestComponentUpdateState();
        technologyGroupLookup.setEnabled(technologyGroupEnabled);
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
            technologyIdForMultiUpload.requestComponentUpdateState();
        } else {
            technologyIdForMultiUpload.setFieldValue("");
            technologyIdForMultiUpload.requestComponentUpdateState();
        }

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
        boolean isTemplateAccepted = technology.getBooleanField(TechnologyFields.IS_TEMPLATE_ACCEPTED);
        boolean isSaved = Objects.nonNull(technologyForm.getEntityId());
        boolean isDraft = TechnologyState.DRAFT.getStringValue().equals(state);

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

    private void fillCriteriaModifiers(final ViewDefinitionState viewDefinitionState) {
        LookupComponent product = (LookupComponent) viewDefinitionState.getComponentByReference("product");
        LookupComponent qualityCard = (LookupComponent) viewDefinitionState.getComponentByReference("qualityCard");

        if (Objects.nonNull(product.getEntity())) {
            FilterValueHolder filter = qualityCard.getFilterValue();

            filter.put(QualityCardCriteriaModifiers.L_PRODUCT_ID, product.getEntity().getId());

            qualityCard.setFilterValue(filter);
            qualityCard.requestComponentUpdateState();
        }
        GridComponent operationComponents = (GridComponent) viewDefinitionState.getComponentByReference(L_OPERATION_COMPONENTS);
        FilterValueHolder operationComponentsFilterValueHolder = operationComponents.getFilterValue();

        operationComponentsFilterValueHolder.put(TechnologyDetailsCriteriaModifiers.L_TECHNOLOGY_ID,
                ((FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM)).getEntityId());

        operationComponents.setFilterValue(operationComponentsFilterValueHolder);

        GridComponent technologicalProcessComponents = (GridComponent) viewDefinitionState
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

}
