package com.qcadoo.mes.masterOrders.listeners;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.qcadoo.model.api.BigDecimalUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeHelperFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanOrdersGroupEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanOrdersGroupHelperFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.masterOrders.states.SalesPlanServiceMarker;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(SalesPlanServiceMarker.class, view, args);
    }

    public void createOrderGroup(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);
        Entity salesPlan = salesPlanForm.getEntity().getDataDefinition().get(salesPlanForm.getEntityId());

        Entity salesPlanOrdersGroupHelper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_HELPER)
                .create();
        salesPlanOrdersGroupHelper.setField(SalesPlanOrdersGroupHelperFields.SALES_PLAN, salesPlanForm.getEntityId());
        salesPlanOrdersGroupHelper = salesPlanOrdersGroupHelper.getDataDefinition().save(salesPlanOrdersGroupHelper);
        Entity finalSalesPlanOrdersGroupHelper = salesPlanOrdersGroupHelper;

        productsGrid.getSelectedEntitiesIds().forEach(salesPlanProductDtoId -> {
            Entity salesPlanProduct = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT)
                    .get(salesPlanProductDtoId);
            Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()
                    .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
                Entity salesPlanOrdersGroupEntry = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                        MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_ENTRY_HELPER).create();
                Entity salesPlanProductDto = dataDefinitionService
                        .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT_DTO)
                        .get(salesPlanProductDtoId);
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.SALES_PLAN_ORDERS_GROUP_HELPER,
                        finalSalesPlanOrdersGroupHelper);
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT, product);
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.TECHNOLOGY,
                        salesPlanProduct.getBelongsToField(SalesPlanProductFields.TECHNOLOGY));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT_FAMILY,
                        product.getBelongsToField(ProductFields.PARENT));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDERED_QUANTITY,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.ORDERED_QUANTITY)
                                .add(BigDecimalUtils.convertNullToZero(salesPlanProduct.getDecimalField(SalesPlanProductFields.ORDERED_TO_WAREHOUSE))));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PLANNED_QUANTITY,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));
                BigDecimal orderQuantity = salesPlanProductDto.getDecimalField("plannedQuantity")
                        .subtract(salesPlanProductDto.getDecimalField("ordersPlannedQuantity"), MathContext.DECIMAL64);
                if (orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    orderQuantity = BigDecimal.ZERO;
                }
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDER_QUANTITY, orderQuantity);

                salesPlanOrdersGroupEntry = salesPlanOrdersGroupEntry.getDataDefinition().save(salesPlanOrdersGroupEntry);
            } else {
                List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);
                for (Entity child : children) {
                    if (Objects.nonNull(child.getBelongsToField(ProductFields.SIZE))) {
                        Entity salesPlanOrdersGroupEntry = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                                MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_ENTRY_HELPER).create();

                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.SALES_PLAN_ORDERS_GROUP_HELPER,
                                finalSalesPlanOrdersGroupHelper);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT, child);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.TECHNOLOGY,
                                salesPlanProduct.getBelongsToField(SalesPlanProductFields.TECHNOLOGY));
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT_FAMILY, product);

                        BigDecimal moProductQuantity = getMasterOrderProductQuantity(salesPlan, child);

                        BigDecimal orderedQuantity = getOrderedQuantity(salesPlan, child);

                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDERED_QUANTITY,
                                moProductQuantity);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PLANNED_QUANTITY,
                                salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));

                        BigDecimal orderQuantity = moProductQuantity.subtract(orderedQuantity, MathContext.DECIMAL64);
                        if (orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
                            orderQuantity = BigDecimal.ZERO;
                        }
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDER_QUANTITY, orderQuantity);

                        salesPlanOrdersGroupEntry = salesPlanOrdersGroupEntry.getDataDefinition().save(salesPlanOrdersGroupEntry);
                    }
                }
            }

        });

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", salesPlanOrdersGroupHelper.getId());

        String url = "../page/masterOrders/salesPlanOrdersGroup.html";
        view.openModal(url, parameters);
    }

    private BigDecimal getMasterOrderProductQuantity(Entity salesPlan, Entity child) {
        StringBuilder moHql = new StringBuilder();
        moHql.append("SELECT moProduct.masterOrderQuantity as moProductQuantity ");
        moHql.append("FROM #masterOrders_masterOrderProduct moProduct ");
        moHql.append("JOIN moProduct.product as product ");
        moHql.append("JOIN moProduct.masterOrder as masterOrder ");
        moHql.append("JOIN masterOrder.salesPlan as salesPlan ");
        moHql.append("WHERE salesPlan.id = :salesPlanId AND product.id = :productId ");
        BigDecimal moProductQuantity = BigDecimal.ZERO;
        List<Entity> moProductQuantityEntities = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT)
                .find(moHql.toString()).setLong("salesPlanId", salesPlan.getId()).setLong("productId", child.getId()).list()
                .getEntities();

        if (!moProductQuantityEntities.isEmpty()) {
            moProductQuantity = moProductQuantityEntities.stream().map(oq -> oq.getDecimalField("moProductQuantity"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return moProductQuantity;
    }

    public void createOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);
        Entity salesPlan = salesPlanForm.getEntity().getDataDefinition().get(salesPlanForm.getEntityId());

        Entity salesPlanOrdersGroupHelper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_HELPER)
                .create();
        salesPlanOrdersGroupHelper.setField(SalesPlanOrdersGroupHelperFields.SALES_PLAN, salesPlanForm.getEntityId());
        salesPlanOrdersGroupHelper = salesPlanOrdersGroupHelper.getDataDefinition().save(salesPlanOrdersGroupHelper);
        Entity finalSalesPlanOrdersGroupHelper = salesPlanOrdersGroupHelper;

        productsGrid.getSelectedEntitiesIds().forEach(salesPlanProductDtoId -> {
            Entity salesPlanProduct = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT)
                    .get(salesPlanProductDtoId);

            Entity salesPlanProductDto = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT_DTO)
                    .get(salesPlanProductDtoId);

            Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()
                    .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
                Entity salesPlanOrdersGroupEntry = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                        MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_ENTRY_HELPER).create();

                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.SALES_PLAN_ORDERS_GROUP_HELPER,
                        finalSalesPlanOrdersGroupHelper);
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT, product);
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.TECHNOLOGY,
                        salesPlanProduct.getBelongsToField(SalesPlanProductFields.TECHNOLOGY));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT_FAMILY,
                        product.getBelongsToField(ProductFields.PARENT));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDERED_QUANTITY,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.ORDERED_QUANTITY)
                                .add(BigDecimalUtils.convertNullToZero(salesPlanProduct.getDecimalField(SalesPlanProductFields.ORDERED_TO_WAREHOUSE))));
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PLANNED_QUANTITY,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));

                BigDecimal orderQuantity = salesPlanProductDto.getDecimalField("plannedQuantity")
                        .subtract(salesPlanProductDto.getDecimalField("ordersPlannedQuantity"), MathContext.DECIMAL64);
                if (orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    orderQuantity = BigDecimal.ZERO;
                }
                salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDER_QUANTITY, orderQuantity);

                salesPlanOrdersGroupEntry = salesPlanOrdersGroupEntry.getDataDefinition().save(salesPlanOrdersGroupEntry);
            } else {
                List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);
                for (Entity child : children) {
                    if (Objects.nonNull(child.getBelongsToField(ProductFields.SIZE))) {
                        Entity salesPlanOrdersGroupEntry = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                                MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_ENTRY_HELPER).create();

                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.SALES_PLAN_ORDERS_GROUP_HELPER,
                                finalSalesPlanOrdersGroupHelper);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT, child);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.TECHNOLOGY,
                                salesPlanProduct.getBelongsToField(SalesPlanProductFields.TECHNOLOGY));
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT_FAMILY, product);

                        BigDecimal moProductQuantity = getMasterOrderProductQuantity(salesPlan, child);

                        BigDecimal orderedQuantity = getOrderedQuantity(salesPlan, child);

                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDERED_QUANTITY,
                                moProductQuantity);
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.PLANNED_QUANTITY,
                                salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));

                        BigDecimal orderQuantity = moProductQuantity.subtract(orderedQuantity, MathContext.DECIMAL64);
                        if (orderQuantity.compareTo(BigDecimal.ZERO) < 0) {
                            orderQuantity = BigDecimal.ZERO;
                        }
                        salesPlanOrdersGroupEntry.setField(SalesPlanOrdersGroupEntryHelperFields.ORDER_QUANTITY, orderQuantity);

                        salesPlanOrdersGroupEntry = salesPlanOrdersGroupEntry.getDataDefinition().save(salesPlanOrdersGroupEntry);
                    }
                }
            }

        });

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", salesPlanOrdersGroupHelper.getId());

        String url = "../page/masterOrders/salesPlanOrders.html";
        view.openModal(url, parameters);
    }

    private BigDecimal getOrderedQuantity(Entity salesPlan, Entity child) {
        BigDecimal orderedQuantity = BigDecimal.ZERO;

        String hql = "SELECT o.plannedQuantity as orderedQuantity " + "FROM #orders_order o "
                + "WHERE o.salesPlan.id = :salesPlanId AND o.product.id = :productId ";

        List<Entity> orderedQuantityEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find(hql)
                .setLong("salesPlanId", salesPlan.getId()).setLong("productId", child.getId()).list().getEntities();
        if (!orderedQuantityEntity.isEmpty()) {
            orderedQuantity = orderedQuantityEntity.stream().map(oq -> oq.getDecimalField("orderedQuantity"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (BigDecimal.ZERO.compareTo(orderedQuantity) > 0) {
                orderedQuantity = BigDecimal.ZERO;
            }
        }
        return orderedQuantity;
    }

    public void openPositionsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity salesPlan = form.getPersistedEntityWithIncludedFormValues();

        Long salesPlanId = salesPlan.getId();

        if (Objects.nonNull(salesPlanId)) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", salesPlanId);

            JSONObject context = new JSONObject(parameters);

            String url = "../page/masterOrders/salesPlanProductsImport.html?context=" + context;
            view.openModal(url);
        }
    }

    public void addProductsBySize(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity helper = getProductsBySizeHelperDD().create();

        helper.setField(ProductsBySizeHelperFields.SALES_PLAN, salesPlanForm.getEntityId());

        helper = helper.getDataDefinition().save(helper);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", helper.getId());

        String url = "../page/masterOrders/productsBySize.html";
        view.openModal(url, parameters);
    }

    public void getOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity salesPlan = salesPlanForm.getPersistedEntityWithIncludedFormValues();

        if (Objects.nonNull(salesPlan)) {
            List<Entity> masterOrders = salesPlan.getHasManyField(SalesPlanFields.MASTER_ORDERS);

            DataDefinition salesPlanProductDD = getSalesPlanProductDD();

            for (Entity salesPlanProduct : salesPlan.getHasManyField(SalesPlanFields.PRODUCTS)) {
                Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
                BigDecimal orderedQuantity = getOrderedQuantity(masterOrders, product, false);
                BigDecimal orderedToWarehouse = getOrderedQuantity(masterOrders, product, true);

                salesPlanProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, orderedQuantity);
                salesPlanProduct.setField(SalesPlanProductFields.ORDERED_TO_WAREHOUSE, orderedToWarehouse);
                salesPlanProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY).subtract(orderedQuantity)
                                .subtract(orderedToWarehouse));

                salesPlanProductDD.save(salesPlanProduct);
            }

            view.addMessage("masterOrders.getOrderedQuantities.success", ComponentState.MessageType.SUCCESS);
        }
    }

    private BigDecimal getOrderedQuantity(final List<Entity> masterOrders, final Entity product, boolean warehouseOrder) {
        BigDecimal orderedQuantity = BigDecimal.ZERO;

        for (Entity masterOrder : masterOrders) {
            if (warehouseOrder == masterOrder.getBooleanField(MasterOrderFields.WAREHOUSE_ORDER)) {
                List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);

                BigDecimal productQuantity = getProductQuantity(product, masterOrderProducts);

                if (Objects.isNull(productQuantity)) {
                    continue;
                }

                orderedQuantity = orderedQuantity.add(productQuantity);
                orderedQuantity = getQuantityForFamily(product, orderedQuantity, masterOrderProducts, productQuantity);
            }
        }

        return orderedQuantity;
    }

    private BigDecimal getQuantityForFamily(final Entity product, BigDecimal orderedQuantity,
            final List<Entity> masterOrderProducts, final BigDecimal productQuantity) {
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))
                && productQuantity.compareTo(BigDecimal.ZERO) == 0) {
            for (Entity child : product.getHasManyField(ProductFields.CHILDREN)) {
                for (Entity masterOrderProduct : masterOrderProducts) {
                    if (child.getId().equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                        orderedQuantity = orderedQuantity
                                .add(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));

                        break;
                    }
                }
            }
        }

        return orderedQuantity;
    }

    private BigDecimal getProductQuantity(final Entity product, final List<Entity> masterOrderProducts) {
        BigDecimal productQuantity = BigDecimal.ZERO;

        for (Entity masterOrderProduct : masterOrderProducts) {
            if (product.getId().equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                productQuantity = masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY);

                break;
            }
        }

        return productQuantity;
    }

    public void showOrderedProductsForFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", productsGrid.getSelectedEntitiesIds().stream().findFirst().get());

        String url = "../page/masterOrders/orderedProductsForFamily.html";
        view.openModal(url, parameters);
    }

    public void useOtherTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("oldTechnologyId",
                dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT)
                        .get(productsGrid.getSelectedEntities().stream().findFirst().get().getId())
                        .getBelongsToField(SalesPlanProductFields.TECHNOLOGY).getId());
        parameters.put("salesPlanProductsIds",
                productsGrid.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        String url = "../page/masterOrders/salesPlanUseOtherTechnology.html";
        view.openModal(url, parameters);
    }

    public void fillTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("productFamilyId",
                dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT)
                        .get(productsGrid.getSelectedEntities().stream().findFirst().get().getId())
                        .getBelongsToField(SalesPlanProductFields.PRODUCT).getBelongsToField(ProductFields.PARENT).getId());
        parameters.put("salesPlanProductsIds",
                productsGrid.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        String url = "../page/masterOrders/salesPlanFillTechnology.html";
        view.openModal(url, parameters);
    }

    public void createSalesPlanMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long salesPlanId = salesPlanForm.getEntityId();

        if (Objects.nonNull(salesPlanId)) {
            Entity salesPlan = getSalesPlan(salesPlanId);

            Entity salesPlanMaterialRequirement = createSalesPlanMaterialRequirement(salesPlan);

            Long salesPlanMaterialRequirementId = salesPlanMaterialRequirement.getId();

            if (Objects.nonNull(salesPlanMaterialRequirementId)) {
                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("form.id", salesPlanMaterialRequirementId);

                parameters.put(L_WINDOW_ACTIVE_MENU, "orders.salesPlanMaterialRequirementsList");

                String url = "../page/masterOrders/salesPlanMaterialRequirementDetails.html";
                view.redirectTo(url, false, true, parameters);
            }
        }
    }

    private Entity createSalesPlanMaterialRequirement(final Entity salesPlan) {
        Entity salesPlanMaterialRequirement = getSalesPlanMaterialRequirementDD().create();

        salesPlanMaterialRequirement.setField(SalesPlanMaterialRequirementFields.SALES_PLAN, salesPlan);

        salesPlanMaterialRequirement = salesPlanMaterialRequirement.getDataDefinition().save(salesPlanMaterialRequirement);

        return salesPlanMaterialRequirement;
    }

    private Entity getSalesPlan(final Long salesPlanId) {
        return getSalesPlanDD().get(salesPlanId);
    }

    private DataDefinition getSalesPlanDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN);
    }

    private DataDefinition getSalesPlanProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT);
    }

    private DataDefinition getSalesPlanMaterialRequirementDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT);
    }

    private DataDefinition getProductsBySizeHelperDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER);
    }

}
