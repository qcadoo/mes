package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveredProductFieldsDTMF;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveredProductHooksDTMF {

    public boolean validateDates(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        Date productionDate = deliveredProduct.getDateField(DeliveredProductFieldsDTMF.PRODUCTION_DATE);
        Date expirationDate = deliveredProduct.getDateField(DeliveredProductFieldsDTMF.EXPIRATION_DATE);
        if (productionDate.compareTo(expirationDate) > 1) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFieldsDTMF.EXPIRATION_DATE),
                    "materialFlow.error.position.expirationDate.lessThenProductionDate");
            return false;
        }
        return true;
    }

}
