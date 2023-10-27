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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SalesVolumeHooks {

    @Autowired
    private NumberService numberService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public boolean validatesWith(final DataDefinition salesVolumeDD, final Entity salesVolume) {
        return checkIfSalesVolumeAlreadyExists(salesVolumeDD, salesVolume);
    }

    public boolean checkIfSalesVolumeAlreadyExists(final DataDefinition salesVolumeDD, final Entity salesVolume) {
        Entity product = salesVolume.getBelongsToField(SalesVolumeFields.PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = salesVolumeDD.find();

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(SalesVolumeFields.PRODUCT, product));

        Long salesVolumeId = salesVolume.getId();

        if (Objects.nonNull(salesVolumeId)) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", salesVolumeId));
        }

        Entity salesVolumeFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(salesVolumeFromDB)) {
            return true;
        } else {
            salesVolume.addError(salesVolumeDD.getField(SalesVolumeFields.PRODUCT),
                    "masterOrders.salesVolume.error.productAlreadyExists");

            return false;
        }
    }

}
