package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.catNumbersInDeliveries.contants.OrderedProductFieldsCNID;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderedProductDetailsHooksCNID {

    private static final String L_FORM = "form";

    private static final String L_SUPPLIER = "supplier";

    @Autowired
    private ProductCatalogNumbersService productCatalogNumbersService;

    public void setCatalogProductNumber(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            return;
        }
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);
        Entity product = productLookup.getEntity();
        if (product == null) {
            return;
        }
        Entity orderedProduct = form.getEntity();
        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);
        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);

        Entity productCatalogNumber = productCatalogNumbersService.getProductCatalogNumber(product, supplier);
        if (productCatalogNumber == null) {
            return;
        }
        FieldComponent productCatalogNumberField = (FieldComponent) view
                .getComponentByReference(OrderedProductFieldsCNID.PRODUCT_CATALOG_NUMBER);
        FieldComponent supplierField = (FieldComponent) view.getComponentByReference(L_SUPPLIER);

        supplierField.setFieldValue(supplier.getStringField(CompanyFields.NAME));
        supplierField.requestComponentUpdateState();
        productCatalogNumberField.setFieldValue(productCatalogNumber.getStringField(ProductCatalogNumberFields.CATALOG_NUMBER));
        productCatalogNumberField.requestComponentUpdateState();
    }
}
