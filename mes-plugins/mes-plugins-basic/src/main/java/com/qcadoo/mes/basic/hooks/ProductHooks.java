package com.qcadoo.mes.basic.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductHooks {

    public void clearFamilyFromProductWhenTypeIsChanged(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity productFromDB = entity.getDataDefinition().get(entity.getId());
        String entityType = entity.getStringField(ProductFields.ENTITY_TYPE);
        if (entityType.equals(ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue())
                && !entityType.equals(productFromDB.getStringField(ProductFields.ENTITY_TYPE))) {
            deleteProductFamily(productFromDB);
        }

    }

    private void deleteProductFamily(final Entity product) {
        DataDefinition productDD = product.getDataDefinition();
        List<Entity> productsWithFamily = productDD.find().add(SearchRestrictions.belongsTo("parent", product)).list()
                .getEntities();
        for (Entity entity : productsWithFamily) {
            entity.setField("parent", null);
            productDD.save(entity);
        }
    }
}
