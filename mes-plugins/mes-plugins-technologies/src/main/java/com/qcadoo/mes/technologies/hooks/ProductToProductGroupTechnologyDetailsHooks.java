package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.mes.technologies.criteriaModifiers.OrderProductCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductToProductGroupTechnologyDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent productFamilyLookup = (LookupComponent) view
                .getComponentByReference(ProductToProductGroupFields.PRODUCT_FAMILY);
        LookupComponent orderProductLookup = (LookupComponent) view
                .getComponentByReference(ProductToProductGroupFields.ORDER_PRODUCT);
        toggleOrderProductEditable(productFamilyLookup, orderProductLookup);

        setCriteriaModifierParameters(productFamilyLookup, orderProductLookup);
    }

    public void toggleOrderProductEditable(LookupComponent productFamilyLookup, LookupComponent orderProductLookup) {
        orderProductLookup.setEnabled(productFamilyLookup.getEntity() != null);
    }

    public void setCriteriaModifierParameters(LookupComponent productFamilyLookup, LookupComponent orderProductLookup) {
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
