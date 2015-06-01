package com.qcadoo.mes.deliveries.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductDetailsListenersD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CompanyProductService companyProductService;

    public void checkIfDefaultSupplierIsUnique(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        Entity formProduct = form.getPersistedEntityWithIncludedFormValues();
        Entity product = productDD.get(formProduct.getId());
        Entity formParent = formProduct.getBelongsToField(ProductFields.PARENT);
        if (formParent != null) {
            Entity parent = productDD.get(formParent.getId());
            boolean familyHasDefault = companyProductService.checkIfDefaultExistsForProductFamily(parent);
            boolean productHasDefault;
            if (familyHasDefault) {
                ProductFamilyElementType productType = ProductFamilyElementType.from(product);
                if (productType.compareTo(ProductFamilyElementType.PARTICULAR_PRODUCT) == 0) {
                    productHasDefault = companyProductService.checkIfDefaultExistsForParticularProduct(product);
                } else {
                    productHasDefault = companyProductService.checkIfDefaultExistsForProductFamily(product);
                }
                if (productHasDefault) {
                    formProduct.addError(productDD.getField(ProductFields.PARENT),
                            "basic.company.message.defaultAlreadyExistsForProductAndFamily");
                    form.setEntity(formProduct);
                }
            }

        }

    }
}
