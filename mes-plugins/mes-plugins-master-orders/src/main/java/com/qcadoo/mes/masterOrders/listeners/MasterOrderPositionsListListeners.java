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
package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.GeneratingOrdersHelperFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.helpers.MasterOrderPositionsHelper;
import com.qcadoo.mes.masterOrders.helpers.SalesPlanMaterialRequirementHelper;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterOrderPositionsListListeners {

    private static final String L_GENERATED = "generated";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_CREATE_COLLECTIVE_ORDERS = "createCollectiveOrders";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrdersFromMOProductsGenerationService ordersGenerationService;

    @Autowired
    private MasterOrderPositionsHelper masterOrderPositionsHelper;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private SalesPlanMaterialRequirementHelper salesPlanMaterialRequirementHelper;

    @Autowired
    private NumberService numberService;

    @Transactional
    public void createDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> masterOrderPositions = grid.getSelectedEntities();

        if (!masterOrderPositions.isEmpty()) {
            Entity parameter = parameterService.getParameter();
            Entity delivery = createDelivery(masterOrderPositions, parameter);

            if (delivery.isValid()) {
                Long deliveryId = delivery.getId();

                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("form.id", deliveryId);

                parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

                String url = "../page/deliveries/deliveryDetails.html";
                view.redirectTo(url, false, true, parameters);
            } else {
                delivery.getErrors().keySet().stream().filter(DeliveryFields.SUPPLIER::equals).findAny().ifPresent(fieldName -> {
                    if (parameter.getBooleanField(ParameterFieldsD.REQUIRE_SUPPLIER_IDENTIFICATION)) {
                        view.addMessage("deliveries.delivery.supplier.isRequired", ComponentState.MessageType.FAILURE);
                    }
                });

                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
    }

    private Entity createDelivery(final List<Entity> masterOrderPositions, Entity parameter) {
        Entity delivery = deliveriesService.getDeliveryDD().create();

        Entity supplier = getSupplier(masterOrderPositions).orElse(null);

        Set<Long> productIds = masterOrderPositions.stream().map(e -> e.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID)
                .longValue()).collect(Collectors.toSet());
        List<Entity> products = getProductsDD().find().add(SearchRestrictions.in("id", productIds)).list().getEntities();
        Set<Long> parentIds = salesPlanMaterialRequirementHelper.getParentIds(products);

        List<Entity> companyProducts = salesPlanMaterialRequirementHelper.getCompanyProducts(productIds, supplier);
        List<Entity> companyProductsFamilies = salesPlanMaterialRequirementHelper.getCompanyProducts(parentIds, supplier);

        List<Entity> orderedProducts = createOrderedProducts(masterOrderPositions, companyProducts,
                companyProductsFamilies);

        if (!orderedProducts.isEmpty()) {
            String number = numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER,
                    DeliveriesConstants.MODEL_DELIVERY);

            delivery.setField(DeliveryFields.NUMBER, number);
            delivery.setField(DeliveryFields.SUPPLIER, supplier);
            Entity currency = null;
            if (supplier != null) {
                currency = supplier.getBelongsToField(CompanyFieldsD.CURRENCY);
            }
            if (currency == null) {
                currency = parameter.getBelongsToField(ParameterFields.CURRENCY);
            }
            delivery.setField(DeliveryFields.CURRENCY, currency);
            delivery.setField(DeliveryFields.LOCATION, parameter.getBelongsToField(ParameterFieldsD.LOCATION));
            delivery.setField(DeliveryFields.DELIVERY_ADDRESS, deliveriesService.getDeliveryAddressDefaultValue());
            delivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
            delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);

            delivery = delivery.getDataDefinition().save(delivery);
        }

        return delivery;
    }

    private List<Entity> createOrderedProducts(List<Entity> masterOrderPositions, List<Entity> companyProducts,
                                               List<Entity> companyProductsFamilies) {
        List<Entity> orderedProducts = Lists.newArrayList();

        Map<Entity, BigDecimal> quantityMap = Maps.newHashMap();
        Map<Entity, BigDecimal> currentStockMap = Maps.newHashMap();
        Map<Entity, BigDecimal> minimumOrderQuantityMap = Maps.newHashMap();

        masterOrderPositions.forEach(masterOrderPosition -> {
            Entity product = getProductsDD().get(masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID).longValue());
            BigDecimal quantity = masterOrderPosition.getDecimalField(MasterOrderPositionDtoFields.MASTER_ORDER_QUANTITY);

            quantityMap.merge(product, quantity, BigDecimal::add);
            currentStockMap.putIfAbsent(product, masterOrderPosition.getDecimalField(MasterOrderPositionDtoFields.WAREHOUSE_STATE));
            minimumOrderQuantityMap.putIfAbsent(product, BigDecimalUtils
                    .convertNullToZero(salesPlanMaterialRequirementHelper.getMinimumOrderQuantity(product, companyProducts, companyProductsFamilies)));
        });
        for (Map.Entry<Entity, BigDecimal> entry : quantityMap.entrySet()) {
            BigDecimal conversion = salesPlanMaterialRequirementHelper.getConversion(entry.getKey());
            BigDecimal orderedQuantity = getOrderedQuantity(entry.getValue(), currentStockMap.get(entry.getKey()), minimumOrderQuantityMap.get(entry.getKey()));
            BigDecimal additionalQuantity = numberService.setScaleWithDefaultMathContext(orderedQuantity.multiply(conversion, numberService.getMathContext()));

            Entity orderedProduct = deliveriesService.getOrderedProductDD().create();

            orderedProduct.setField(OrderedProductFields.PRODUCT, entry.getKey());
            orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
            orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);

            orderedProducts.add(orderedProduct);
        }

        return orderedProducts;
    }

    private BigDecimal getOrderedQuantity(final BigDecimal quantity, final BigDecimal currentStock,
                                          final BigDecimal minimumOrderQuantity) {
        BigDecimal orderedQuantity = quantity.subtract(currentStock, numberService.getMathContext());

        if (BigDecimal.ZERO.compareTo(orderedQuantity) >= 0) {
            orderedQuantity = BigDecimal.ZERO;
        } else if (orderedQuantity.compareTo(minimumOrderQuantity) < 0) {
            orderedQuantity = minimumOrderQuantity;
        }

        return orderedQuantity;
    }

    private Optional<Entity> getSupplier(final List<Entity> masterOrderPositions) {
        Optional<Entity> masterOrderPosition = masterOrderPositions.stream()
                .filter(mop -> Objects.nonNull(mop
                        .getStringField(MasterOrderPositionDtoFields.SUPPLIER)))
                .findFirst();
        return masterOrderPosition.map(entity -> getProductsDD().get(entity.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID).longValue()).getBelongsToField(ProductFields.SUPPLIER));
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> selectedEntity = masterOrderPositionGrid.getSelectedEntities();

        if (selectedEntity.isEmpty()) {
            state.addMessage("masterOrders.masterOrder.masterOrdersPosition.lessEntitiesSelectedThanAllowed",
                    ComponentState.MessageType.INFO);

            return;
        } else if (selectedEntity.size() != 1) {
            state.addMessage("masterOrders.masterOrder.masterOrdersPosition.moreEntitiesSelectedThanAllowed",
                    ComponentState.MessageType.INFO);

            return;
        }

        Entity masterOrderPosition = selectedEntity.get(0);

        Integer masterOrderId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.MASTER_ORDER_ID);

        if (Objects.isNull(masterOrderId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.masterOrder", masterOrderId);

        Integer productId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID);
        Integer masterOrderProductId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.MASTER_ORDER_PRODUCT_ID);

        parameters.put("form.masterOrderProduct", productId);
        parameters.put("form.masterOrderProductComponent", masterOrderProductId);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void goToGenerateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selected = masterOrderPositionGrid.getSelectedEntitiesIds();

        boolean createCollectiveOrders = parameterService.getParameter().getBooleanField(L_CREATE_COLLECTIVE_ORDERS);

        if (createCollectiveOrders) {
            Entity generatingOrders = getGeneratingOrdersHelperDD().create();
            generatingOrders.setField(GeneratingOrdersHelperFields.SELECTED_ENTITIES,
                    selected.stream().map(Object::toString).collect(Collectors.joining(",")));
            generatingOrders = generatingOrders.getDataDefinition().save(generatingOrders);

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", generatingOrders.getId());

            String url = "../page/masterOrders/generatingOrders.html";
            view.openModal(url, parameters);
        } else {
            List<Entity> masterOrderProducts = getMasterOrderPositionDtoDD().find().add(SearchRestrictions.in("id", selected))
                    .list().getEntities();

            ordersGenerationService.generateOrders(masterOrderProducts, null, null, true).showMessage(view);

            state.performEvent(view, "reset");
        }
    }

    public void showGroupedByProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        String url = "../page/masterOrders/masterOrderPositionsGroupedByProductList.html";
        view.redirectTo(url, false, true);

    }

    public void showGroupedByProductAndDate(final ViewDefinitionState view, final ComponentState state,
                                            final String[] args) {
        String url = "../page/masterOrders/masterOrderPositionsGroupedByProductAndDateList.html";
        view.redirectTo(url, false, true);

    }

    public void updateWarehouseStateAndDelivery(final ViewDefinitionState view, final ComponentState state,
                                                final String[] args) {
        List<Entity> masterOrderProducts = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find().list().getEntities();
        if (!masterOrderProducts.isEmpty()) {
            Entity parameter = parameterService.getParameter();
            masterOrderPositionsHelper.updateDeliveriesProductQuantities(view, masterOrderProducts, parameter);
            masterOrderPositionsHelper.updateWarehouseStates(masterOrderProducts, parameter);

            view.addMessage("masterOrders.masterOrderPositionsList.updateWarehouseStateAndDelivery.success",
                    ComponentState.MessageType.SUCCESS);
        }
    }

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Entity helper = masterOrderForm.getPersistedEntityWithIncludedFormValues();
        String selectedEntities = helper.getStringField(GeneratingOrdersHelperFields.SELECTED_ENTITIES);

        Date start = helper.getDateField(GeneratingOrdersHelperFields.START_DATE);
        Date finish = helper.getDateField(GeneratingOrdersHelperFields.FINISH_DATE);

        List<Long> ids = Lists.newArrayList(selectedEntities.split(",")).stream().map(Long::valueOf).collect(Collectors.toList());

        List<Entity> masterOrderProducts = getMasterOrderPositionDtoDD().find().add(SearchRestrictions.in("id", ids)).list()
                .getEntities();

        ordersGenerationService.generateOrders(masterOrderProducts, start, finish, true).showMessage(view);

        generatedCheckBox.setChecked(true);
    }

    public void openMasterOrdersImportPage(final ViewDefinitionState view, final ComponentState state,
                                           final String[] args) {
        view.openModal("../page/masterOrders/masterOrdersImport.html");
    }

    private DataDefinition getGeneratingOrdersHelperDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.GENERATING_ORDERS_HELPER);
    }

    private DataDefinition getMasterOrderPositionDtoDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO);
    }

    private DataDefinition getProductsDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PRODUCT);
    }

}
