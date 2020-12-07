package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.stereotype.Service;

@Service
public class CompanyProductsDetailsHooks {

    private static final String L_UNIT = "unit";

    public void onBeforeRenderForProduct(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity product = companyForm.getEntity().getBelongsToField("product");
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);
        unitField.setFieldValue(product.getStringField(ProductFields.UNIT));
        unitField.requestComponentUpdateState();
    }

    public void onBeforeRenderForCompany(final ViewDefinitionState view) {

        LookupComponent productLookupComponent = (LookupComponent) view.getComponentByReference("product");
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);
        if (productLookupComponent.isEmpty()) {
            unitField.setFieldValue("");
        } else {
            unitField.setFieldValue(productLookupComponent.getEntity().getStringField(ProductFields.UNIT));
        }
        unitField.requestComponentUpdateState();
    }
}
