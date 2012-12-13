package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.basic.constants.CompanyFields.NAME;
import static com.qcadoo.mes.catNumbersInDeliveries.contants.OrderedProductFieldsCNID.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderedProductDetailsHooksCNID {

    private static final String L_FORM = "form";

    @Autowired
    private ProductCatalogNumbersService productCatalogNumbersService;

    public void setCatalogProductNumber(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity orderedProduct = form.getEntity();

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();

        if (product != null) {
            Entity delivery = orderedProduct.getBelongsToField(DELIVERY);
            Entity supplier = delivery.getBelongsToField(SUPPLIER);

            Entity productCatalogNumber = productCatalogNumbersService.getProductCatalogNumber(product, supplier);

            if (productCatalogNumber != null) {
                FieldComponent productCatalogNumberField = (FieldComponent) view.getComponentByReference(PRODUCT_CATALOG_NUMBER);
                FieldComponent supplierField = (FieldComponent) view.getComponentByReference(SUPPLIER);

                supplierField.setFieldValue(supplier.getStringField(NAME));
                supplierField.requestComponentUpdateState();

                productCatalogNumberField.setFieldValue(productCatalogNumber.getStringField(CATALOG_NUMBER));
                productCatalogNumberField.requestComponentUpdateState();
            }
        }
    }

}
