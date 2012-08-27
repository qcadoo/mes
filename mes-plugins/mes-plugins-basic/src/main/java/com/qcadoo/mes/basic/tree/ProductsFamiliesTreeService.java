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
