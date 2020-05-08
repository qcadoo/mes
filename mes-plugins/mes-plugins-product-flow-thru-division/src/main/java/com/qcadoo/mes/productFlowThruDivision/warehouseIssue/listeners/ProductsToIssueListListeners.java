package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.ProductsToIssueService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductsToIssueListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductsToIssueService productsToIssueService;

    public void showProductAttributes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent positionGird = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> ids = positionGird.getSelectedEntitiesIds();
        if (ids.size() == 1) {
            Entity productToIssue = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                    ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE).get(ids.stream().findFirst().get());
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT).getId());
            view.redirectTo("/page/materialFlowResources/productAttributesForPositionList.html", false, true, parameters);
        } else {
            view.addMessage("materialFlow.info.document.showProductAttributes.toManyPositionsSelected",
                    ComponentState.MessageType.INFO);
        }
    }

    public void correctReservations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        List<Long> selectedEntities = Lists.newArrayList(grid.getSelectedEntitiesIds());
        if (selectedEntities.isEmpty()) {
            view.addMessage("productFlowThruDivision.productsToIssueList.noSelectedEntities", ComponentState.MessageType.INFO);
            return;
        }
        Entity firstProduct = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE).get(selectedEntities.get(0));

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productsToIssueIds", selectedEntities.stream().map(Object::toString)
                .collect(Collectors.joining(",")));
        if (firstProduct != null) {
            parameters.put("form.locationFrom", firstProduct.getBelongsToField(ProductsToIssueFields.LOCATION).getId());
            parameters.put("form.placeOfIssue", firstProduct.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)
                    .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE).getId());
        }
        String url = "../page/productFlowThruDivision/productToIssueCorrectionHelperDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void fillStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productsToIssueService.fillStorageLocations();
        view.addMessage("productFlowThruDivision.productsToIssueList.fillStorageLocations.success",
                ComponentState.MessageType.SUCCESS);
    }

    public void copyProductsToIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        List<Long> selectedEntities = Lists.newArrayList(grid.getSelectedEntitiesIds());
        if (selectedEntities.isEmpty()) {
            view.addMessage("productFlowThruDivision.productsToIssueList.noSelectedEntities", ComponentState.MessageType.INFO);
            return;
        }
        productsToIssueService.fillStorageLocations(selectedEntities);
        Entity firstProduct = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE).get(selectedEntities.get(0));

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productsToIssueIds", selectedEntities.stream().map(Object::toString)
                .collect(Collectors.joining(",")));
        if (firstProduct != null) {
            parameters.put("form.locationFrom", firstProduct.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)
                    .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE).getId());
        }
        Optional.ofNullable(grid.getFilters().get("productNumber")).ifPresent(
                value -> parameters.put("form.gridProductNumberFilter", value));
        String url = "../page/productFlowThruDivision/productsToIssueHelperDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    private void changeRibbonState(final ViewDefinitionState view) {

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup group = window.getRibbon().getGroupByName("warehouseIssues");
        RibbonActionItem copyProducts = group.getItemByName("copyProducts");
        RibbonActionItem correctReservations = group.getItemByName("correctReservations");
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        DataDefinition productsToIssueDD = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
        Set<Long> selectedEntitiesIds = grid.getSelectedEntitiesIds();
        boolean enabled = !selectedEntitiesIds.isEmpty();
        boolean correctionEnabled = !selectedEntitiesIds.isEmpty();
        if (enabled) {
            Entity warehouse = null;
            Entity location = null;
            for (Long id : selectedEntitiesIds) {
                Entity productToIssue = productsToIssueDD.get(id);
                if (productToIssue != null) {
                    Entity warehouseIssue = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE);
                    String state = warehouseIssue.getStringField(WarehouseIssueFields.STATE);
                    Entity productLocation = productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION);
                    if (location == null) {
                        location = productLocation;
                    } else {
                        if (productLocation != null && !location.getId().equals(productLocation.getId())) {
                            correctionEnabled = false;
                        }
                    }
                    if (WarehouseIssueState.DISCARD.getStringValue().equals(state)
                            || WarehouseIssueState.COMPLETED.getStringValue().equals(state)) {
                        enabled = false;
                    } else {
                        Entity issueWarehouse = warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);
                        if (warehouse == null) {
                            warehouse = issueWarehouse;
                        } else {
                            if (!warehouse.getId().equals(issueWarehouse.getId())) {
                                enabled = false;
                                correctionEnabled = false;
                            }
                        }
                    }
                } else {
                    enabled = false;
                }
            }
        }
        correctReservations.setEnabled(correctionEnabled);
        correctReservations.requestUpdate(true);
        copyProducts.setEnabled(enabled);
        copyProducts.requestUpdate(true);
    }

    public void onBeforeRender(final ViewDefinitionState view) {
        changeRibbonState(view);
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
    }

    public void changeRibbonState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        changeRibbonState(view);
    }
}
