/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basic.tree;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;

@Service
public class ProductsFamiliesTreeService {

    public List<Entity> getHierarchyProductsTree(final Entity product) {
        List<Entity> tree = new ArrayList<Entity>();
        addProduct(tree, product);
        generateTree(product, tree);
        return tree;
    }

    private void addProduct(final List<Entity> tree, final Entity child) {
        child.setField(ProductFields.PRIORITY, 1);
        tree.add(child);
    }

    private List<Entity> generateTree(final Entity product, final List<Entity> tree) {
        List<Entity> productsChild = product.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS);
        for (Entity productEntity : productsChild) {
            addProduct(tree, productEntity);
            if (productEntity.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS) != null
                    && !productEntity.getHasManyField(ProductFields.PRODUCT_FAMILY_CHILDRENS).isEmpty()) {
                generateTree(productEntity, tree);
            }
        }
        return tree;
    }
}
