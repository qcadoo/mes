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
