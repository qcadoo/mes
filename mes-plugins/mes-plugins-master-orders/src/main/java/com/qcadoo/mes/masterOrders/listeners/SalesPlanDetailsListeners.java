package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesPlanDetailsListeners {

    public void addProductsBySize(final ViewDefinitionState view, final ComponentState state, final String[] args) {
    }

    public void getOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity salesPlan = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getPersistedEntityWithIncludedFormValues();
        if (salesPlan != null) {
            List<Entity> masterOrders = salesPlan.getHasManyField(SalesPlanFields.MASTER_ORDERS);
            for (Entity salesPlanProduct : salesPlan.getHasManyField(SalesPlanFields.PRODUCTS)) {
                Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
                BigDecimal orderedQuantity = getOrderedQuantity(masterOrders, product);
                if (orderedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    salesPlanProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, orderedQuantity);
                    salesPlanProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN,
                            salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY).subtract(orderedQuantity));
                    salesPlanProduct.getDataDefinition().save(salesPlanProduct);
                } else if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                        .equals(product.getField(ProductFields.ENTITY_TYPE))) {
                    orderedQuantity = BigDecimal.ZERO;
                    for (Entity child : product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS)) {
                        orderedQuantity = orderedQuantity.add(getOrderedQuantity(masterOrders, child));
                    }
                    if (orderedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        salesPlanProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, orderedQuantity);
                        salesPlanProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN, salesPlanProduct
                                .getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY).subtract(orderedQuantity));
                        salesPlanProduct.getDataDefinition().save(salesPlanProduct);
                    }
                }
            }
        }
    }

    private BigDecimal getOrderedQuantity(List<Entity> masterOrders, Entity product) {
        BigDecimal orderedQuantity = BigDecimal.ZERO;
        for (Entity masterOrder : masterOrders) {
            List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);
            for (Entity masterOrderProduct : masterOrderProducts) {
                if (product.getId().equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                    orderedQuantity = orderedQuantity
                            .add(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
                }
            }
        }
        return orderedQuantity;
    }

    public void showOrderedProductsForFamily(final ViewDefinitionState view, final ComponentState state, final String[] args) {
    }
}
