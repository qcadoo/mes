package com.qcadoo.mes.deliveriesToMaterialFlow.aop.helper;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveredProductFieldsDTMF;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveredProductValidationHelper {

    public boolean checkDeliveredProductUniqueness(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder scb = deliveredProductDD
                .find()
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.eq(DeliveredProductFieldsDTMF.BATCH,
                        deliveredProduct.getField(DeliveredProductFieldsDTMF.BATCH)))
                .add(SearchRestrictions.eq(DeliveredProductFieldsDTMF.EXPIRATION_DATE,
                        deliveredProduct.getField(DeliveredProductFieldsDTMF.EXPIRATION_DATE)))
                .add(SearchRestrictions.eq(DeliveredProductFieldsDTMF.PRODUCTION_DATE,
                        deliveredProduct.getField(DeliveredProductFieldsDTMF.PRODUCTION_DATE)));
        Entity result = scb.setMaxResults(1).uniqueResult();
        return result == null;
    }
}
