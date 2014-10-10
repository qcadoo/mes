/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.EAN;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.tree.ProductNumberingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductHooks {

    @Autowired
    private ProductNumberingService productNumberingService;

    @Autowired
    private ProductService productService;

    public void generateNodeNumber(final DataDefinition productDD, final Entity product) {
        productNumberingService.generateNodeNumber(product);
    }

    public void updateNodeNumber(final DataDefinition productDD, final Entity product) {
        productNumberingService.updateNodeNumber(product);
    }

    public void clearFamilyFromProductWhenTypeIsChanged(final DataDefinition productDD, final Entity product) {
        if (product.getId() == null) {
            return;
        }
        String entityType = product.getStringField(ENTITY_TYPE);
        Entity productFromDB = product.getDataDefinition().get(product.getId());
        if (entityType.equals(PARTICULAR_PRODUCT.getStringValue())
                && !entityType.equals(productFromDB.getStringField(ENTITY_TYPE))) {
            deleteProductFamily(productFromDB);
        }
    }

    private void deleteProductFamily(final Entity product) {
        DataDefinition productDD = product.getDataDefinition();
        List<Entity> productsWithFamily = productDD.find().add(SearchRestrictions.belongsTo(PARENT, product)).list()
                .getEntities();
        for (Entity entity : productsWithFamily) {
            entity.setField("parent", null);
            productDD.save(entity);
        }
    }

    public boolean checkIfNotBelongsToSameFamily(final DataDefinition productDD, final Entity product) {
        if (product.getId() != null) {
            Entity parent = product.getBelongsToField(PARENT);

            if ((parent != null) && product.getId().equals(parent.getId())) {
                product.addError(productDD.getField(PARENT), "basic.product.parent.belongsToSameFamily");

                return false;
            }
        }

        return true;
    }

    public boolean checkIfParentIsFamily(final DataDefinition productDD, final Entity product) {
        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (parent == null) {
            return true;
        }
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(parent.getStringField(ProductFields.ENTITY_TYPE))) {
            return true;
        } else {
            product.addError(productDD.getField(PARENT), "basic.product.parent.parentIsNotFamily");

            return false;
        }
    }

    public void clearEanOnCopy(final DataDefinition dataDefinition, final Entity product) {
        if (product == null) {
            return;
        }
        product.setField(EAN, null);
    }

    public boolean checkIfEanValueIsNumerical(final DataDefinition productDD, final Entity product) {
        String eanCode = product.getStringField(EAN);
        if (eanCode == null || StringUtils.isNumeric(eanCode)) {
            return true;
        } else {
            product.addError(productDD.getField(EAN), "basic.product.ean.onlyNumericValue");
            return false;
        }
    }

    public void clearExternalIdOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        if (entity == null) {
            return;
        }
        entity.setField("externalNumber", null);
    }

    public void calculateConversionIfUnitChanged(final DataDefinition productDD, final Entity product) {
        if (productService.hasUnitChangedOnUpdate(product)) {
            productService.conversionForProductUnit(product);
        }
    }

    public void calculateConversionOnCreate(final DataDefinition productDD, final Entity product) {
        productService.conversionForProductUnit(product);
    }

}
