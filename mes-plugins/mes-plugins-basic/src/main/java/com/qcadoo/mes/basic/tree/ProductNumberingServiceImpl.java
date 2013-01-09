/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.NODE_NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;
import static com.qcadoo.mes.basic.constants.ProductFields.PRODUCT_FAMILY_CHILDRENS;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductNumberingServiceImpl implements ProductNumberingService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity getRoot(final Entity product) {
        Entity parent = product.getBelongsToField(PARENT);

        if (parent == null) {
            return product;
        } else {
            return getRoot(parent);
        }
    }

    @Override
    public void generateNodeNumber(final Entity product) {
        Entity parent = product.getBelongsToField(PARENT);

        generateNodeNumber(product, parent, false);
    }

    private void generateNodeNumber(final Entity product, final Entity parent, final boolean doSave) {
        String entityType = product.getStringField(ENTITY_TYPE);

        if (PRODUCTS_FAMILY.getStringValue().equals(entityType)) {
            generateNodeNumberForProductsFamily(product, parent);
        } else if (PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
            generateNodeNumberForParticularProduct(product, parent);
        }

        if (doSave) {
            product.getDataDefinition().save(product);
        }
    }

    private void generateNodeNumberForParticularProduct(final Entity product, final Entity parent) {
        if (parent == null) {
            product.setField(NODE_NUMBER, null);
        } else {
            product.setField(NODE_NUMBER, getNextNodeNumber(parent));
        }
    }

    private void generateNodeNumberForProductsFamily(final Entity product, final Entity parent) {
        if (parent == null) {
            product.setField(NODE_NUMBER, getNextRootNodeNumber());
        } else {
            product.setField(NODE_NUMBER, getNextNodeNumber(parent));
        }
    }

    @Override
    public void updateNodeNumber(final Entity product) {
        Entity parent = product.getBelongsToField(PARENT);

        updateNodeNumber(product, parent);
    }

    private void updateNodeNumber(final Entity product, final Entity parent) {
        String entityType = product.getStringField(ENTITY_TYPE);

        if (product.getId() != null) {
            if (checkIfParentHasChanged(product, parent)) {
                if (checkIfEntityTypeHasChanged(product, entityType)) {
                    if (PRODUCTS_FAMILY.getStringValue().equals(entityType)) {
                        generateNodeNumberForProductsFamily(product, parent);
                    } else if (PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                        generateNodeNumberForParticularProduct(product, parent);

                        updateProductFamilyChildrensNodeNumbers(product, null);
                    }
                } else {
                    if (PRODUCTS_FAMILY.getStringValue().equals(entityType)) {
                        generateNodeNumberForProductsFamily(product, parent);

                        updateProductFamilyChildrensNodeNumbers(product, product);
                    } else if (PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                        generateNodeNumberForParticularProduct(product, parent);
                    }
                }
            } else {
                if (checkIfEntityTypeHasChanged(product, entityType)) {
                    if (PRODUCTS_FAMILY.getStringValue().equals(entityType)) {
                        generateNodeNumberForProductsFamily(product, parent);
                    } else if (PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                        updateProductFamilyChildrensNodeNumbers(product, null);
                    }
                }
            }
        }
    }

    private void updateProductFamilyChildrensNodeNumbers(final Entity product, final Entity parent) {
        List<Entity> productFamilyChildrens = product.getHasManyField(PRODUCT_FAMILY_CHILDRENS);

        for (Entity productFamilyChildren : productFamilyChildrens) {
            generateNodeNumber(productFamilyChildren, parent, true);

            updateProductFamilyChildrensNodeNumbers(productFamilyChildren, productFamilyChildren);
        }
    }

    private String getNextRootNodeNumber() {
        Entity product = getProductDD().find().add(SearchRestrictions.isNull(PARENT))
                .add(SearchRestrictions.isNotNull(NODE_NUMBER)).addOrder(SearchOrders.desc(NODE_NUMBER)).setMaxResults(1)
                .uniqueResult();

        String nodeNumber = null;
        Integer number = null;

        if (product == null) {
            number = 1;

            nodeNumber = number.toString();
        } else {
            String productNodeNumber = product.getStringField(NODE_NUMBER);

            number = Integer.parseInt(productNodeNumber);

            do {
                number++;

                nodeNumber = number.toString();
            } while (checkIfNodeNumberIsUnique(nodeNumber));
        }

        return nodeNumber;
    }

    private String getNextNodeNumber(final Entity parent) {
        String parentNodeNumber = parent.getStringField(NODE_NUMBER);

        Entity product = parent.getHasManyField(PRODUCT_FAMILY_CHILDRENS).find()
                .add(SearchRestrictions.like(NODE_NUMBER, parentNodeNumber + "%")).addOrder(SearchOrders.desc(NODE_NUMBER))
                .setMaxResults(1).uniqueResult();

        String nodeNumber = null;
        Integer number = null;

        if (product == null) {
            number = 1;

            nodeNumber = parentNodeNumber.concat(".").concat(number.toString());
        } else {
            String productNodeNumber = product.getStringField(NODE_NUMBER);

            number = Integer.parseInt(productNodeNumber.replace(parentNodeNumber.concat("."), ""));

            do {
                number++;

                nodeNumber = parentNodeNumber.concat(".").concat(number.toString());
            } while (checkIfNodeNumberIsUnique(nodeNumber));
        }

        return nodeNumber;
    }

    private boolean checkIfNodeNumberIsUnique(final String nodeNumber) {
        return getProductDD().find().add(SearchRestrictions.eq(NODE_NUMBER, nodeNumber)).setMaxResults(1).uniqueResult() != null;
    }

    private boolean checkIfParentHasChanged(final Entity product, final Entity parent) {
        Entity existingProduct = getProductDD().get(product.getId());

        Entity existingParent = existingProduct.getBelongsToField(PARENT);

        if ((parent == null) && (existingParent == null)) {
            return false;
        } else if (existingParent == null) {
            return true;
        } else {
            return !existingParent.equals(parent);
        }
    }

    private boolean checkIfEntityTypeHasChanged(final Entity product, final String entityType) {
        Entity existingProduct = getProductDD().get(product.getId());

        String existingEntityType = existingProduct.getStringField(ENTITY_TYPE);

        return !existingEntityType.equals(entityType);
    }

    @Override
    public boolean checkIfProductBelongsToProductsFamily(final Entity productsFamily, final Entity product) {
        String productsFamilyNodeNumber = productsFamily.getStringField(NODE_NUMBER);
        String productNodeNumber = product.getStringField(NODE_NUMBER);

        if (StringUtils.isEmpty(productNodeNumber)) {
            return false;
        } else {
            return productNodeNumber.startsWith(productsFamilyNodeNumber);
        }
    }

    @Override
    public List<Entity> getProductRoots(final Entity product) {
        String productNodeNumber = product.getStringField(NODE_NUMBER);

        if (StringUtils.isEmpty(productNodeNumber)) {
            return null;
        } else {
            return getProductDD().find().add(SearchRestrictions.in(NODE_NUMBER, findRootsForNodeNumber(productNodeNumber)))
                    .list().getEntities();
        }
    }

    private List<String> findRootsForNodeNumber(final String nodeNumber) {
        List<String> roots = Lists.newArrayList();

        String number = nodeNumber;

        for (int i = 0; i < StringUtils.countMatches(nodeNumber, "."); i++) {
            number = number.substring(0, number.lastIndexOf('.'));

            roots.add(number);
        }

        return roots;
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
