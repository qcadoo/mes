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
package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderedProductConfiguratorAttributeFields;
import com.qcadoo.mes.technologies.services.ProductDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderedProductConfiguratorAttributeDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductDataService productDataService;

    public void createOrderedProductConfiguratorAttributes(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent orderedProductConfiguratorAttributeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent attributesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity orderedProductConfiguratorAttribute = orderedProductConfiguratorAttributeForm.getEntity();

        List<Entity> attributes = attributesGrid.getSelectedEntities();

        Entity orderedProductConfigurator = orderedProductConfiguratorAttribute.getBelongsToField(OrderedProductConfiguratorAttributeFields.ORDERED_PRODUCT_CONFIGURATOR);

        attributes.forEach(attribute -> createOrderedProductConfiguratorAttribute(orderedProductConfigurator, attribute));
    }

    private void createOrderedProductConfiguratorAttribute(Entity orderedProductConfigurator, Entity attribute) {
        Entity orderedProductConfiguratorAttribute = getOrderedProductConfiguratorAttribute().create();

        orderedProductConfiguratorAttribute.setField(OrderedProductConfiguratorAttributeFields.ORDERED_PRODUCT_CONFIGURATOR, orderedProductConfigurator);
        orderedProductConfiguratorAttribute.setField(OrderedProductConfiguratorAttributeFields.ATTRIBUTE, attribute);

        orderedProductConfiguratorAttribute.getDataDefinition().save(orderedProductConfiguratorAttribute);
    }

    private DataDefinition getOrderedProductConfiguratorAttribute() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_ORDERED_PRODUCT_CONFIGURATOR_ATTRIBUTE);
    }

}
