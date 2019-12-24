package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.criteriaModifiers.OrderProductCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductToProductGroupTechnologyDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_ORDER_PRODUCT = "orderProduct";

    private static final String L_PRODUCT_FAMILY = "productFamily";

    public void onBeforeRender(final ViewDefinitionState view) {
        toggleOrderProductEditable(view);

        setCriteriaModifierParameters(view);
    }

    public void toggleOrderProductEditable(ViewDefinitionState view) {
        LookupComponent productFamilyLookup = (LookupComponent) view.getComponentByReference(L_PRODUCT_FAMILY);
        view.getComponentByReference(L_ORDER_PRODUCT).setEnabled(productFamilyLookup.getEntity() != null);
    }

    public void setCriteriaModifierParameters(ViewDefinitionState view) {
        LookupComponent productFamilyLookup = (LookupComponent) view.getComponentByReference(L_PRODUCT_FAMILY);
        LookupComponent orderProductLookup = (LookupComponent) view.getComponentByReference(L_ORDER_PRODUCT);

        Entity productFamily = productFamilyLookup.getEntity();
        Entity orderProduct = orderProductLookup.getEntity();

        FilterValueHolder filterValueHolder = orderProductLookup.getFilterValue();

        if (productFamily == null) {
            filterValueHolder.remove(OrderProductCriteriaModifiers.L_PRODUCT_FAMILY_ID);

            orderProductLookup.setFieldValue(null);
        } else {
            Long productFamilyId = productFamily.getId();

            filterValueHolder.put(OrderProductCriteriaModifiers.L_PRODUCT_FAMILY_ID, productFamilyId);

            if (orderProduct != null && !productFamilyId.equals(orderProduct.getBelongsToField(ProductFields.PARENT).getId())) {
                orderProductLookup.setFieldValue(null);
            }
        }

        orderProductLookup.setFilterValue(filterValueHolder);
        orderProductLookup.requestComponentUpdateState();
    }
}
