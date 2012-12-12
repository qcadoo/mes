/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD.OPERATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class OrderedProductHooksTSFDOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER);
    }

    public boolean checkIfOrderedProductAlreadyExists(final DataDefinition orderedProductDD, final Entity orderedProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = orderedProductDD.find()
                .add(SearchRestrictions.belongsTo(DELIVERY, orderedProduct.getBelongsToField(DELIVERY)))
                .add(SearchRestrictions.belongsTo(PRODUCT, orderedProduct.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(OPERATION, orderedProduct.getBelongsToField(OPERATION)));

        if (orderedProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", orderedProduct.getId()));
        }

        Entity orderedProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (orderedProductFromDB == null) {
            return true;
        } else {
            orderedProduct.addError(orderedProductDD.getField(PRODUCT), "deliveries.orderedProduct.error.alreadyExists");

            return false;
        }
    }

}
