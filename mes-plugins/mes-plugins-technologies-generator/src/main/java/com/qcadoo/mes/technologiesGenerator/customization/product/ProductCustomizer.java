/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.technologiesGenerator.customization.product;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.ProductFieldsTG;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNameSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNumberSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductCustomizer {

    @Autowired
    private CustomizedProductDataProvider customizedProductDataProvider;

    public Entity findOrCreate(final Entity product, final Entity mainProduct, final ProductSuffixes suffixes,
                               final GeneratorSettings settings) {
        String generatedNumber = generateNumber(product, suffixes.getNumberSuffix());

        return customizedProductDataProvider.tryFind(product, generatedNumber).orElseGet(() -> customize(product, mainProduct,
                generatedNumber, generateName(product, suffixes.getNameSuffix()), settings));
    }

    private String generateNumber(final Entity product, final ProductNumberSuffix numberSuffix) {
        String originalNumber = product.getStringField(ProductFields.NUMBER);

        return String.format("%s - %s", originalNumber, numberSuffix.get());
    }

    private String generateName(final Entity product, final ProductNameSuffix nameSuffix) {
        String originalName = product.getStringField(ProductFields.NAME);

        return String.format("%s - %s", originalName, nameSuffix.get());
    }

    private Entity customize(final Entity product, final Entity mainProduct, final String newNumber, final String newName,
                             final GeneratorSettings settings) {
        DataDefinition productDD = product.getDataDefinition();

        Entity newProduct = productDD.copy(product.getId()).get(0);

        newProduct.setField(ProductFields.PARENT, product);
        newProduct.setField(ProductFields.NAME, newName);
        newProduct.setField(ProductFields.NUMBER, newNumber);
        newProduct.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        newProduct.setField(ProductFieldsTG.FROM_GENERATOR, true);

        if (settings.shouldCopyProductSize() && Objects.nonNull(mainProduct)) {
            newProduct.setField(ProductFields.SIZE, mainProduct.getBelongsToField(ProductFields.SIZE));
        }

        if (settings.shouldCopyProductAttributes() && Objects.nonNull(mainProduct)) {
            newProduct.setField(ProductFields.PRODUCT_ATTRIBUTE_VALUES, copyProductAttributeValues(mainProduct));
        }

        return productDD.save(newProduct);
    }

    private List<Entity> copyProductAttributeValues(final Entity product) {
        List<Entity> productAttributeValues = Lists.newArrayList();

        for (Entity productAttributeValue : product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES)) {
            Entity productAttributeValueCopy  = productAttributeValue.copy();

            productAttributeValueCopy.setId(null);
            productAttributeValueCopy.setField(ProductAttributeValueFields.PRODUCT, null);

            productAttributeValues.add(productAttributeValueCopy);
        }

        return productAttributeValues;
    }

}
