package com.qcadoo.mes.masterOrders.listeners;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

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
                BigDecimal orderedQuantity = getOrderedQuantity(masterOrders, product);

                salesPlanProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, orderedQuantity);
                salesPlanProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN,
                        salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY).subtract(orderedQuantity));

                salesPlanProductDD.save(salesPlanProduct);
            }

            view.addMessage("masterOrders.getOrderedQuantities.success", ComponentState.MessageType.SUCCESS);
        }
    }

    private BigDecimal getOrderedQuantity(final List<Entity> masterOrders, final Entity product) {
        BigDecimal orderedQuantity = BigDecimal.ZERO;

        for (Entity masterOrder : masterOrders) {
            List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);

            BigDecimal productQuantity = getProductQuantity(product, masterOrderProducts);

            if (Objects.isNull(productQuantity)) {
                continue;
            }

            orderedQuantity = orderedQuantity.add(productQuantity);
            orderedQuantity = getQuantityForFamily(product, orderedQuantity, masterOrderProducts, productQuantity);
        }

        return orderedQuantity;
    }

    private BigDecimal getQuantityForFamily(final Entity product, BigDecimal orderedQuantity,
            final List<Entity> masterOrderProducts, final BigDecimal productQuantity) {
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))
                && productQuantity.compareTo(BigDecimal.ZERO) == 0) {
            for (Entity child : product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS)) {
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

    public void createSalesPlanMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long salesPlanId = salesPlanForm.getEntityId();

        if (Objects.nonNull(salesPlanId)) {
            Entity salesPlan = getSalesPlan(salesPlanId);

            Entity delivery = createSalesPlanMaterialRequirement(salesPlan);

            Long deliveryId = delivery.getId();

            if (Objects.nonNull(deliveryId)) {
                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("form.id", deliveryId);

                parameters.put(L_WINDOW_ACTIVE_MENU, "orders.salesPlanMaterialRequirementsList");

                String url = "../page/masterOrders/salesPlanMaterialRequirementDetails.html";
                view.redirectTo(url, false, true, parameters);
            }
        }
    }

    private Entity createSalesPlanMaterialRequirement(final Entity salesPlan) {
        Entity salesPlanMaterialRequirement = getSalesPlanMaterialRequirementDD().create();

        String number = numberGeneratorService.generateNumber(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT);

        salesPlanMaterialRequirement.setField(SalesPlanMaterialRequirementFields.NUMBER, number);
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
