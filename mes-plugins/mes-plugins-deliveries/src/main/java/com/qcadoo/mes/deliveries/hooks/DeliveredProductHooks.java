package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveredProductHooks {

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition dataDefinition, final Entity entity) {
        Entity orderedProductFromDB = dataDefinition.find()
                .add(SearchRestrictions.belongsTo(DELIVERY, entity.getBelongsToField(DELIVERY)))
                .add(SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT))).uniqueResult();
        if (orderedProductFromDB == null) {
            return true;
        } else {
            entity.addError(dataDefinition.getField(PRODUCT), "deliveries.delivedProduct.error.alreadyExists");
            return false;
        }
    }
}
