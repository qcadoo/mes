package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveredProductHooks {

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinition.find()
                .add(SearchRestrictions.belongsTo(DELIVERY, entity.getBelongsToField(DELIVERY)))
                .add(SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT)));

        if (entity.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", entity.getId()));
        }
        Entity orderedProductFromDB = searchCriteriaBuilder.uniqueResult();
        if (orderedProductFromDB == null) {
            return true;
        } else {
            entity.addError(dataDefinition.getField(PRODUCT), "deliveries.delivedProduct.error.alreadyExists");
            return false;
        }
    }

    public boolean checkIfDeliveredQuantityIsLessThanDamagedQuantity(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY).compareTo(
                entity.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY)) == 1) {
            entity.addError(dataDefinition.getField(DeliveredProductFields.DAMAGED_QUANTITY),
                    "deliveries.delivedProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");
            entity.addError(dataDefinition.getField(DeliveredProductFields.DELIVERED_QUANTITY),
                    "deliveries.delivedProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");
            return false;
        }
        return true;
    }

}
