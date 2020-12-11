package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SalesPlanDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addProductsBySize(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER).create();
        helper.setField(ProductsBySizeHelperFields.SALES_PLAN, form.getEntityId());
        helper = helper.getDataDefinition().save(helper);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", helper.getId());

        String url = "../page/masterOrders/productsBySize.html";
        view.openModal(url, parameters);
    }

    public void getOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity salesPlan = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getPersistedEntityWithIncludedFormValues();
        if (salesPlan != null) {
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
        }
    }

    private BigDecimal getOrderedQuantity(List<Entity> masterOrders, Entity product) {
        BigDecimal orderedQuantity = BigDecimal.ZERO;
        for (Entity masterOrder : masterOrders) {
            List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);
            BigDecimal productQuantity = BigDecimal.ZERO;
            for (Entity masterOrderProduct : masterOrderProducts) {
                if (product.getId().equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                    productQuantity = masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY);
                    break;
                }
            }
            if (productQuantity == null) {
                continue;
            }
            orderedQuantity = orderedQuantity.add(productQuantity);
            if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))
                    && productQuantity.compareTo(BigDecimal.ZERO) == 0) {
                for (Entity child : product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS)) {
                    for (Entity masterOrderProduct : masterOrderProducts) {
                        if (child.getId()
                                .equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                            orderedQuantity = orderedQuantity
                                    .add(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
                            break;
                        }
                    }
                }
            }
        }
        return orderedQuantity;
    }

    public void showOrderedProductsForFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", gridComponent.getSelectedEntitiesIds().stream().findFirst().get());

        String url = "../page/masterOrders/orderedProductsForFamily.html";
        view.openModal(url, parameters);
    }

    private DataDefinition getSalesPlanProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT);
    }
}
