package com.qcadoo.mes.basic.tree;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductsFamiliesTreeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getHierarchyProductsTree(final Entity product) {
        List<Entity> tree = new ArrayList<Entity>();
        List<Entity> children = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(ProductFields.PRODUCT_FAMILY_PARENT, product)).list().getEntities();
        for (Entity child : children) {
            tree.add(child);
        }
        return tree;
    }
}
