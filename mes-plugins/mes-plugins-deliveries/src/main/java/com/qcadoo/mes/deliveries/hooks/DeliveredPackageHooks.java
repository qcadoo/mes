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
package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.deliveries.constants.DeliveredPackageFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeliveredPackageHooks {

    public boolean validatesWith(final DataDefinition deliveredPackageDD, final Entity deliveredPackage) {
        return checkIfDeliveredPackageAlreadyExists(deliveredPackageDD, deliveredPackage);
    }

    public boolean checkIfDeliveredPackageAlreadyExists(final DataDefinition deliveredPackageDD, final Entity deliveredPackage) {
        Entity delivery = deliveredPackage.getBelongsToField(DeliveredPackageFields.DELIVERY);
        Entity product = deliveredPackage.getBelongsToField(DeliveredPackageFields.PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = deliveredPackageDD.find();

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT, product));

        Long deliveredPackageId = deliveredPackage.getId();

        if (Objects.nonNull(deliveredPackageId)) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredPackageId));
        }

        Entity deliveredPackageFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(deliveredPackageFromDB)) {
            return true;
        } else {
            deliveredPackage.addError(deliveredPackageDD.getField(DeliveredPackageFields.PRODUCT),
                    "deliveries.deliveredPackage.error.productAlreadyExists");

            return false;
        }
    }

}
