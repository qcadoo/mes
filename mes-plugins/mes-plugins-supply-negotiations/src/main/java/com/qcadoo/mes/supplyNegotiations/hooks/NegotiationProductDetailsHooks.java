/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.supplyNegotiations.hooks;

import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.PRODUCT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class NegotiationProductDetailsHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("neededQuantityUnit", "approvedDeliveredQuantityUnit",
                "draftDeliveredQuantityUnit", "leftQuantityUnit");

        deliveriesService.fillUnitFields(view, PRODUCT, referenceNames);
    }

}
