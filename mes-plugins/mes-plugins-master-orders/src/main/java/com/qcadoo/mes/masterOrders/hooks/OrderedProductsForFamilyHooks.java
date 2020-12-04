package com.qcadoo.mes.masterOrders.hooks;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderedProductsForFamilyHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        Entity salesPlanProduct = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getPersistedEntityWithIncludedFormValues();
        Entity product = salesPlanProduct.getBelongsToField(SalesPlanProductFields.PRODUCT);
        FieldComponent productField = (FieldComponent) view.getComponentByReference(SalesPlanProductFields.PRODUCT);
        productField
                .setFieldValue(product.getStringField(ProductFields.NUMBER) + ", " + product.getStringField(ProductFields.NAME));
        productField.requestComponentUpdateState();
        FieldComponent plannedQuantityField = (FieldComponent) view
                .getComponentByReference(SalesPlanProductFields.PLANNED_QUANTITY);
        plannedQuantityField
                .setFieldValue(numberService.format(salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY))
                        + " " + product.getStringField(ProductFields.UNIT));
        plannedQuantityField.requestComponentUpdateState();
        List<Entity> masterOrders = salesPlanProduct.getBelongsToField(SalesPlanProductFields.SALES_PLAN)
                .getHasManyField(SalesPlanFields.MASTER_ORDERS);
        List<Entity> salesPlanProducts = Lists.newArrayList();
        DataDefinition salesPlanProductDD = getSalesPlanProductDD();
        for (Entity child : product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS).stream()
                .sorted(Comparator.comparing(e -> e.getStringField(ProductFields.NUMBER))).collect(Collectors.toList())) {
            Entity salesPlanProductChild = salesPlanProductDD.create();
            salesPlanProductChild.setField(SalesPlanProductFields.PRODUCT, child);
            BigDecimal orderedQuantity = BigDecimal.ZERO;
            for (Entity masterOrder : masterOrders) {
                List<Entity> masterOrderProducts = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS);
                for (Entity masterOrderProduct : masterOrderProducts) {
                    if (child.getId().equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())) {
                        orderedQuantity = orderedQuantity
                                .add(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
                        break;
                    }
                }
            }
            salesPlanProductChild.setField(SalesPlanProductFields.ORDERED_QUANTITY, orderedQuantity);
            salesPlanProducts.add(salesPlanProductChild);
        }
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);
        gridComponent.setEntities(salesPlanProducts);
    }

    private DataDefinition getSalesPlanProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT);
    }

}
