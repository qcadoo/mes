/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyProductServiceImpl implements CompanyProductService {

    private static final Logger LOG = LoggerFactory.getLogger(CompanyProductServiceImpl.class);

    @Autowired
    private ProductService productService;

    public boolean checkIfProductIsNotUsed(final Entity companyProduct, final String belongsToProductName,
            final String belongsToCompanyName, final String hasManyName) {
        if (companyProduct.getId() == null) {
            Entity product = companyProduct.getBelongsToField(belongsToProductName);

            if (product == null) {
                return true;
            } else {
                Entity company = companyProduct.getBelongsToField(belongsToCompanyName);

                if (company == null) {
                    return true;
                } else {
                    SearchResult searchResult = company.getHasManyField(hasManyName).find()
                            .add(SearchRestrictions.belongsTo(belongsToProductName, product)).list();

                    return searchResult.getEntities().isEmpty();
                }
            }
        }

        return true;
    }

    public boolean checkIfDefaultAlreadyExists(final Entity companyProduct) {
        if (companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)) {
            Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);

            if (product == null) {
                return false;
            } else {
                Entity productFamily = product.getBelongsToField(ProductFields.PARENT);
                if (productFamily != null) {
                    List<Entity> companyProductsForFamily = productFamily
                            .getHasManyField(ProductFieldsD.PRODUCTS_FAMILY_COMPANIES);
                    if (companyProductsForFamily.stream().anyMatch(
                            companyProductForFamily -> companyProductForFamily.getBooleanField(CompanyProductFields.IS_DEFAULT))) {
                        return true;
                    }
                }
                List<Entity> companyProductsForProduct = product.getHasManyField(ProductFieldsD.PRODUCT_COMPANIES);
                return companyProductsForProduct.stream().anyMatch(
                        companyProductForProduct -> companyProductForProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)
                                && !companyProductForProduct.getId().equals(companyProduct.getId()));
            }
        }

        return false;
    }

    public boolean checkIfDefaultExistsForFamily(final Entity companyProduct) {
        if (companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)) {
            Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);

            if (product == null) {
                return false;
            } else {
                Entity productFamily = product.getBelongsToField(ProductFields.PARENT);
                if (productFamily != null) {
                    List<Entity> companyProductsForFamily = productFamily
                            .getHasManyField(ProductFieldsD.PRODUCTS_FAMILY_COMPANIES);
                    if (companyProductsForFamily.stream().anyMatch(
                            companyProductForFamily -> companyProductForFamily.getBooleanField(CompanyProductFields.IS_DEFAULT))) {
                        return true;
                    }
                }
                List<Entity> companyProductsForProduct = product.getHasManyField(ProductFieldsD.PRODUCTS_FAMILY_COMPANIES);
                return companyProductsForProduct.stream()
                        .anyMatch(
                                companyProductForProduct -> (companyProductForProduct
                                        .getBooleanField(CompanyProductFields.IS_DEFAULT) && !companyProductForProduct.getId()
                                        .equals(companyProduct.getId())));
            }
        }

        return false;
    }

    public String checkIfDefaultExistsForProductsInFamily(final Entity companyProduct) {
        LOG.warn("start");
        if (companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)) {
            Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);

            if (product == null) {
                return StringUtils.EMPTY;
            } else {
                StringBuilder productNames = new StringBuilder();
                List<Entity> children = product.getHasManyField(ProductFields.CHILDREN);
                for (Entity child : children) {
                    List<Entity> productCompanies = child.getHasManyField(ProductFieldsD.PRODUCT_COMPANIES);
                    if (!productCompanies.isEmpty()) {
                        String defaultCompaniesForProducts = productCompanies
                                .stream()
                                .filter(cp -> cp.getBooleanField(CompanyProductFields.IS_DEFAULT))
                                .map(cp -> cp.getBelongsToField(CompanyProductFields.PRODUCT)
                                        .getStringField(ProductFields.NUMBER)).collect(Collectors.joining(", "));
                        productNames.append(defaultCompaniesForProducts);
                    }
                }
                LOG.warn("stop");

                return productNames.toString();
            }
        }
        LOG.warn("stop");

        return StringUtils.EMPTY;
    }

    public boolean checkIfDefaultExistsForParticularProduct(final Entity product) {

        if (product == null) {
            return false;
        } else {
            List<Entity> companyProductsForProduct = product.getHasManyField(ProductFieldsD.PRODUCT_COMPANIES);
            if (companyProductsForProduct.stream().anyMatch(
                    companyProductForProduct -> companyProductForProduct.getBooleanField(CompanyProductFields.IS_DEFAULT))) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfDefaultExistsForProductFamily(final Entity product) {

        if (product == null) {
            return false;
        } else {
            List<Entity> companyProductsForProduct = product.getHasManyField(ProductFieldsD.PRODUCTS_FAMILY_COMPANIES);
            if (companyProductsForProduct.stream().anyMatch(
                    companyProductForProduct -> (companyProductForProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)))) {
                return true;
            }
        }
        return false;

    }
}
