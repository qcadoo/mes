/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveredProductFieldsDTMF;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DeliveredProductHooksDTMF {

    public boolean validateDates(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        Date productionDate = deliveredProduct.getDateField(DeliveredProductFieldsDTMF.PRODUCTION_DATE);
        Date expirationDate = deliveredProduct.getDateField(DeliveredProductFieldsDTMF.EXPIRATION_DATE);
        if (productionDate != null && expirationDate != null) {
            if (productionDate.after(expirationDate)) {
                deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFieldsDTMF.EXPIRATION_DATE),
                        "materialFlow.error.position.expirationDate.lessThenProductionDate");
                return false;
            }
        }

        boolean requireExpirationDate = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)
                .getBelongsToField(DeliveryFields.LOCATION).getBooleanField(LocationFieldsMFR.REQUIRE_EXPIRATION_DATE);

        if (requireExpirationDate && expirationDate == null) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFieldsDTMF.EXPIRATION_DATE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        return true;
    }

}
