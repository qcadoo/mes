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
package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.QualityCardFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.hooks.TechnologyDetailsHooks;
import com.qcadoo.mes.technologies.states.listener.TechnologyValidationService;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.technologies.tree.RemoveTOCService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT_STRUCTURE_TREE;

@Service
public class TechnologyDetailsListeners {

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_PRODUCT_STRUCTURE_FORM = "productStructureForm";

    private static final String L_PRODUCT_STRUCTURE = "productStructure";

    private static final String L_TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    private static final String L_IN_PRODUCTS = "inProducts";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private RemoveTOCService removeTOCService;

    @Autowired
    private TechnologyValidationService technologyValidationService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyDetailsHooks technologyDetailsHooks;

    public void setGridEditable(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }

    public void removeOnlySelectedOperation(final ViewDefinitionState view, final ComponentState state,
                                            final String[] args) {
        final TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(L_TECHNOLOGY_TREE_REFERENCE);
        final Long selectedEntityId = technologyTree.getSelectedEntityId();

        Entity selectedOperation = getTechnologyOperationComponentDD().get(selectedEntityId);

        boolean removed = removeTOCService.removeOnlySelectedOperation(selectedOperation, view);

        if (removed) {
            FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

            technologyForm.performEvent(view, "reset");

            view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.success",
                    ComponentState.MessageType.SUCCESS);
        }
    }

    public void generateProductStructure(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FormComponent productStructureForm = (FormComponent) view.getComponentByReference(L_PRODUCT_STRUCTURE_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Entity technology = technologyForm.getEntity();
        Entity productTechnology = technology.copy();
        EntityTree generatedTree = productStructureTreeService.generateProductStructureTree(view, technology);

        productTechnology.setField(PRODUCT_STRUCTURE_TREE, generatedTree);
        productStructureForm.setEntity(productTechnology);

        window.setActiveTab(L_PRODUCT_STRUCTURE);
    }

    public void moveProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent inProductsGrid = (GridComponent) view.getComponentByReference(L_IN_PRODUCTS);

        if (inProductsGrid.getSelectedEntitiesIds().isEmpty()) {
            state.addMessage("technologies.technologyDetails.window.noSelectedProducts", MessageType.INFO);

            return;
        }

        boolean invalidProducts = false;

        DataDefinition operationProductInComponentDD = getOperationProductInComponentDD();

        for (Entity inProductDto : inProductsGrid.getSelectedEntities()) {
            Entity inProduct = operationProductInComponentDD.get(inProductDto.getId());
            Entity operationComponent = inProduct.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
            Entity parent = operationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT);

            if (Objects.nonNull(parent)) {
                Entity product = inProduct.getBelongsToField(OperationProductInComponentFields.PRODUCT);

                if (Objects.nonNull(product) && technologyService.isIntermediateProduct(inProduct)) {
                    state.addMessage("technologies.technologyDetails.window.noOutProductInOperation", MessageType.INFO,
                            product.getStringField(ProductFields.NUMBER));
                }

                inProduct.setField(OperationProductInComponentFields.OPERATION_COMPONENT, parent);
                inProduct = operationProductInComponentDD.save(inProduct);

                if (!inProduct.isValid()) {
                    invalidProducts = true;
                }
            } else {
                state.addMessage("technologies.technologyDetails.window.operationWithoutParent", MessageType.INFO);

                return;
            }
        }

        if (invalidProducts) {
            state.addMessage("technologies.technologyDetails.window.moveProducts.info", MessageType.INFO);
        } else {
            state.addMessage("technologies.technologyDetails.window.moveProducts.success", MessageType.SUCCESS);
        }
    }

    public void fillProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity technology = getTechnologyDD().get(technologyForm.getEntityId());

        List<Entity> operationsWithManyOutProducts = fillProducts(technology);

        if (!operationsWithManyOutProducts.isEmpty()) {
            state.addMessage("technologies.technologyDetails.window.tooManyOutProductsInOperation", MessageType.INFO,
                    operationsWithManyOutProducts.stream().map(operation -> operation.getStringField(OperationFields.NUMBER))
                            .collect(Collectors.joining(", ")));
        }
    }

    public List<Entity> fillProducts(final Entity technology) {
        List<Entity> technologyOperationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        List<Entity> operationsWithManyOutProducts = Lists.newArrayList();

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            List<Entity> operationProductOutComponents = technologyOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            Entity parent = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT);

            long notWasteCount = operationProductOutComponents.stream()
                    .filter(operationProductOutComponent -> !operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE)).count();

            if (Objects.nonNull(parent)) {
                if (notWasteCount == 1) {
                    Entity operationProductOutComponentNotWaste = operationProductOutComponents.stream()
                            .filter(operationProductOutComponent -> !operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE))
                            .collect(Collectors.toList()).stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException("No operation component"));

                    Entity operationProductInComponent = getOperationProductInComponentDD().create();

                    operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY,
                            operationProductOutComponentNotWaste.getField(OperationProductOutComponentFields.QUANTITY));
                    operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT,
                            operationProductOutComponentNotWaste.getBelongsToField(OperationProductOutComponentFields.PRODUCT));
                    operationProductInComponent.setField(OperationProductInComponentFields.OPERATION_COMPONENT, parent);

                    operationProductInComponent.getDataDefinition().save(operationProductInComponent);
                } else if (operationProductOutComponents.size() > 1) {
                    operationsWithManyOutProducts
                            .add(technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION));
                }
            }
        }

        return operationsWithManyOutProducts;
    }

    public void openOperationProductInComponentsImportPage(final ViewDefinitionState view, final ComponentState state,
                                                           final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity technology = technologyForm.getPersistedEntityWithIncludedFormValues();

        Long technologyId = technology.getId();

        if (Objects.nonNull(technologyId)) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", technologyId);

            JSONObject context = new JSONObject(parameters);

            String url = "../page/technologies/operationProductInComponentsImport.html?context=" + context;

            view.openModal(url);
        }
    }

    public void fillQualityCard(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(TechnologyFields.PRODUCT);
        LookupComponent qualityCardLookup = (LookupComponent) view.getComponentByReference(TechnologyFields.QUALITY_CARD);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            List<Entity> qualityCards = getQualityCardDD().find()
                    .add(SearchRestrictions.eq(QualityCardFields.STATE, "02accepted"))
                    .createAlias(QualityCardFields.PRODUCTS, QualityCardFields.PRODUCTS, JoinType.INNER)
                    .add(SearchRestrictions.eq(QualityCardFields.PRODUCTS + ".id", product.getId())).list().getEntities();

            if (qualityCards.size() == 1) {
                qualityCardLookup.setFieldValue(qualityCards.get(0).getId());
            } else {
                qualityCardLookup.setFieldValue(null);
            }
        }
    }

    public void acceptTemplate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent isTemplateAcceptedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(TechnologyFields.IS_TEMPLATE_ACCEPTED);

        Entity technology = technologyForm.getEntity();

        Long technologyId = technology.getId();

        boolean isTemplateAccepted = isTemplateAcceptedCheckBox.isChecked();

        if (Objects.nonNull(technologyId)) {
            technology = technology.getDataDefinition().save(technology);

            if (isTemplateAccepted || validateTemplate(technologyForm, technology.getDataDefinition().get(technologyId))) {
                isTemplateAcceptedCheckBox.setChecked(!isTemplateAccepted);

                technologyForm.performEvent(view, "save");

                if (technologyForm.isHasError()) {
                    isTemplateAcceptedCheckBox.setChecked(isTemplateAccepted);
                }
            }
        }
    }

    private boolean validateTemplate(final FormComponent technologyForm, final Entity technology) {
        boolean isValid = technologyValidationService.checkIfTechnologyTreeIsSet(technologyForm, technology);

        isValid = isValid
                && technologyValidationService.checkTopComponentsProducesProductForTechnology(technologyForm, technology);
        isValid = isValid && technologyValidationService.checkIfOperationsUsesSubOperationsProds(technologyForm, technology);

        return isValid;
    }

    public void showProductData(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity technology = technologyForm.getEntity();

        if (Objects.isNull(technology.getId())) {
            return;
        }

        String technologyNumber = technology.getStringField(TechnologyFields.NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("technologyNumber", applyInOperator(technologyNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technologies.productDatasList");

        String url = "../page/technologies/productDatasList.html";

        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

    public final void showInProductCart(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        GridComponent inProductsGrid = (GridComponent) view.getComponentByReference(L_IN_PRODUCTS);

        if (inProductsGrid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("technologies.technologyDetails.window.ribbon.productsIn.nonSelected",
                    ComponentState.MessageType.INFO);

            return;
        }

        List<Entity> operationProductInComponentDtos = inProductsGrid.getSelectedEntities();

        for (Entity operationProductInComponentDto : operationProductInComponentDtos) {
            Entity operationProductInComponent = operationProductInComponentDto.getDataDefinition().getMasterModelEntity(operationProductInComponentDto.getId());

            operationProductInComponent.setField(OperationProductInComponentFields.SHOW_IN_PRODUCT_DATA, true);

            operationProductInComponent.getDataDefinition().save(operationProductInComponent);
        }
    }

    public final void setProductDataRibbonState(final ViewDefinitionState view, final ComponentState state,
                                                final String[] args) {
        technologyDetailsHooks.setProductDataRibbonState(view);
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getOperationProductInComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private DataDefinition getQualityCardDD() {
        return dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_QUALITY_CARD);
    }

}
