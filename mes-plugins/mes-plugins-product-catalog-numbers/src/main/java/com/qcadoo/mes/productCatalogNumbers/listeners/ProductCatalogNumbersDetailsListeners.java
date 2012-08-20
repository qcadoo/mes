package com.qcadoo.mes.productCatalogNumbers.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductCatalogNumbersDetailsListeners {

    public void showProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent productLookup = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        Long productId = (Long) productLookup.getFieldValue();
        if (productId != null) {
            String url = "../page/basic/productDetails.html?context={\"form.id\":\"" + productId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }
}
