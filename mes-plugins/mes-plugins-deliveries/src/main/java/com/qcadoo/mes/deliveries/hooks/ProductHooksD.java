package com.qcadoo.mes.deliveries.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductHooksD {

    @Autowired
    private CompanyProductService companyProductService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity product) {
        if (product.getId() != null) {
            Entity productFromDB = dataDefinition.get(product.getId());
            ProductFamilyElementType oldProductType = ProductFamilyElementType.from(productFromDB);
            ProductFamilyElementType newProductType = ProductFamilyElementType.from(product);

            if (oldProductType.compareTo(newProductType) != 0) {
                if (ProductFamilyElementType.PARTICULAR_PRODUCT.compareTo(newProductType) == 0) {
                    moveCompanyProductFamiliesToCompanyProducts(product);
                } else if (ProductFamilyElementType.PRODUCTS_FAMILY.compareTo(newProductType) == 0) {
                    moveCompanyProductsToCompanyProductFamilies(product);
                }
            }
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

    public boolean checkIfDefaultSupplierIsUnique(final DataDefinition productDD, final Entity product) {
        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (parent != null) {
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
                    product.addError(productDD.getField(ProductFields.PARENT),
                            "basic.company.message.defaultAlreadyExistsForProductAndFamily");
                    return false;
                }
            }

        }
        return true;
    }
}
