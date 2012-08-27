package com.qcadoo.mes.basic;

import java.util.ArrayList;
import java.util.List;

import com.qcadoo.model.api.Entity;

public class ConversionFamiliesTreeService {

    public static final String PARENT = "parent";

    public static final String CONVERSION_FAMILY_CHILDRENS = "conversionFamilyChildrens";

    public static final String PRIORITY = "priority";

    public List<Entity> getHierarchyConversionsTree(final Entity conversion) {
        List<Entity> tree = new ArrayList<Entity>();
        addProduct(tree, conversion);
        generateTree(conversion, tree);
        return tree;
    }

    private void addProduct(final List<Entity> tree, final Entity child) {
        child.setField(PRIORITY, 1);
        tree.add(child);
    }

    private List<Entity> generateTree(final Entity conversion, final List<Entity> tree) {

        List<Entity> conversionChild = conversion.getHasManyField(CONVERSION_FAMILY_CHILDRENS);

        for (Entity conversionEntity : conversionChild) {

            addProduct(tree, conversionEntity);

            if (conversionEntity.getHasManyField(CONVERSION_FAMILY_CHILDRENS) != null
                    && !conversionEntity.getHasManyField(CONVERSION_FAMILY_CHILDRENS).isEmpty()) {

                generateTree(conversionEntity, tree);

            }
        }
        return tree;
    }

}
