package com.qcadoo.mes.deliveries.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductDetailsListenersD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateCompanyProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity product = form.getPersistedEntityWithIncludedFormValues();
        ProductFamilyElementType newProductType = ProductFamilyElementType.from(product);

        if (ProductFamilyElementType.PARTICULAR_PRODUCT.compareTo(newProductType) == 0) {
            moveCompanyProductFamiliesToCompanyProducts(product);
        } else if (ProductFamilyElementType.PRODUCTS_FAMILY.compareTo(newProductType) == 0) {
            moveCompanyProductsToCompanyProductFamilies(product);
        }
    }

    private void moveCompanyProductsToCompanyProductFamilies(final Entity particularProduct) {
        DataDefinition companyProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COMPANY_PRODUCT);
        DataDefinition companyProductsFamilyDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COMPANY_PRODUCTS_FAMILY);
        Entity productFromDB = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                particularProduct.getId());
        List<Entity> productCompanies = companyProductDD.find()
                .add(SearchRestrictions.belongsTo(CompanyProductFields.PRODUCT, productFromDB)).list().getEntities();
        for (Entity productCompany : productCompanies) {
            Entity companyProductsFamily = companyProductsFamilyDD.create();
            companyProductsFamily.setField(CompanyProductsFamilyFields.COMPANY,
                    productCompany.getBelongsToField(CompanyProductFields.COMPANY));
            companyProductsFamily.setField(CompanyProductsFamilyFields.PRODUCT, particularProduct);
            companyProductsFamily.setField(CompanyProductsFamilyFields.IS_DEFAULT,
                    productCompany.getBooleanField(CompanyProductFields.IS_DEFAULT));
            companyProductsFamilyDD.save(companyProductsFamily);
            companyProductDD.delete(productCompany.getId());
        }
    }

    private void moveCompanyProductFamiliesToCompanyProducts(final Entity productFamily) {
        DataDefinition companyProductsFamilyDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COMPANY_PRODUCTS_FAMILY);
        DataDefinition companyProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COMPANY_PRODUCT);
        Entity productFromDB = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                productFamily.getId());
        List<Entity> productFamilyCompanies = companyProductsFamilyDD.find()
                .add(SearchRestrictions.belongsTo(CompanyProductsFamilyFields.PRODUCT, productFromDB)).list().getEntities();
        for (Entity productFamilyCompany : productFamilyCompanies) {
            Entity companyProduct = companyProductDD.create();
            companyProduct.setField(CompanyProductFields.COMPANY,
                    productFamilyCompany.getBelongsToField(CompanyProductsFamilyFields.COMPANY));
            companyProduct.setField(CompanyProductFields.PRODUCT, productFamily);
            companyProduct.setField(CompanyProductFields.IS_DEFAULT,
                    productFamilyCompany.getBooleanField(CompanyProductsFamilyFields.IS_DEFAULT));
            companyProductDD.save(companyProduct);
            companyProductsFamilyDD.delete(productFamilyCompany.getId());
        }
    }
}
