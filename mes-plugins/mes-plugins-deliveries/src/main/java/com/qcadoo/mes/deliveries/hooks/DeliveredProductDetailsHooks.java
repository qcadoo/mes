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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveredProductDetailsHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void fillOrderedQuantities(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();
        FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference(ORDERED_QUANTITY);
        if (product == null) {
            orderedQuantity.setFieldValue(null);
            orderedQuantity.requestComponentUpdateState();
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity delivery = form.getEntity().getBelongsToField(DELIVERY);

        Entity orderedProduct = deliveriesService.getOrderedProductDD().find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).uniqueResult();
        orderedQuantity.setFieldValue(orderedProduct.getDecimalField(ORDERED_QUANTITY));
        orderedQuantity.requestComponentUpdateState();
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("damagedQuantityUNIT", "deliveredQuantityUNIT", "orderedQuantityUNIT");

        deliveriesService.fillUnitFields(view, PRODUCT, referenceNames);
    }

    public void setDeliveredQuantityFieldRequired(final ViewDefinitionState view) {
        FieldComponent delivedQuantity = (FieldComponent) view.getComponentByReference(DELIVERED_QUANTITY);
        delivedQuantity.setRequired(true);
        delivedQuantity.requestComponentUpdateState();
    }

}
