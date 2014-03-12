/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.orders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CopyOfTechnologyDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Transactional
    public void changePatternTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FormComponent technologyForm = (FormComponent) state;
        LookupComponent technologyPrototypeLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFields.TECHNOLOGY_PROTOTYPE);

        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            Entity technology = technologyServiceO.getTechnologyDD().get(technologyId);
            Entity order = getOrderWithTechnology(technology);

            Entity orderTechnologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

            Entity technologyPrototype = technologyPrototypeLookup.getEntity();

            Entity copyOfTechnology = technologyServiceO.getTechnologyDD().create();

            if ((technologyPrototype != null) && !technologyPrototype.getId().equals(orderTechnologyPrototype.getId())) {
                order.setField(OrderFields.TECHNOLOGY, null);

                order.getDataDefinition().save(order);

                deleteTechnology(technology);

                copyOfTechnology = copyTechnology(technologyPrototype, order);

                order.setField(OrderFields.TECHNOLOGY, copyOfTechnology);
                order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);

                order.getDataDefinition().save(order);

                state.setFieldValue(copyOfTechnology.getId());

                technologyForm.setEntity(copyOfTechnology);
            }
            // TODO sprawdzić
            // if (copyOfTechnology.getId() == null) {
            // technologyServiceO.setQuantityOfWorkstationTypes(order, technology);
            // } else {
            // technologyServiceO.setQuantityOfWorkstationTypes(order, copyOfTechnology);
            // }
        }
    }

    @Transactional
    public void clearTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FormComponent technologyForm = (FormComponent) state;

        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            Entity technology = technologyServiceO.getTechnologyDD().get(technologyId);
            Entity order = getOrderWithTechnology(technology);

            order.setField(OrderFields.TECHNOLOGY, null);
            order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);

            order.getDataDefinition().save(order);

            deleteTechnology(technology);

            Entity newTechnology = createTechnology(order);

            order.setField(OrderFields.TECHNOLOGY, newTechnology);

            order.getDataDefinition().save(order);

            // TODO sprawdzić
            // technologyServiceO.setQuantityOfWorkstationTypes(order, newTechnology);

            state.setFieldValue(newTechnology.getId());
            technologyForm.setEntity(newTechnology);
        }
    }

    @Transactional
    public void clearAndLoadPatternTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FormComponent technologyForm = (FormComponent) state;

        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            Entity technology = technologyServiceO.getTechnologyDD().get(technologyId);
            Entity order = getOrderWithTechnology(technology);

            order.setField(OrderFields.TECHNOLOGY, null);

            order.getDataDefinition().save(order);

            deleteTechnology(technology);

            Entity orderTechnologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

            Entity copyOfTechnology = copyTechnology(orderTechnologyPrototype, order);

            order.setField(OrderFields.TECHNOLOGY, copyOfTechnology);

            order.getDataDefinition().save(order);

            // TODO sprawdzić
            // technologyServiceO.setQuantityOfWorkstationTypes(order, copyOfTechnology);

            state.setFieldValue(copyOfTechnology.getId());
            technologyForm.setEntity(copyOfTechnology);
        }
    }

    private Entity getOrderWithTechnology(final Entity technology) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).setMaxResults(1).uniqueResult();
    }

    private Entity createTechnology(final Entity order) {
        Entity newTechnology = technologyServiceO.getTechnologyDD().create();

        String number = numberGeneratorService.generateNumber(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        Entity product = order.getBelongsToField(TechnologyFields.PRODUCT);

        newTechnology.setField(TechnologyFields.NUMBER, number);
        newTechnology.setField(TechnologyFields.NAME, technologyServiceO.makeTechnologyName(number, product));
        newTechnology.setField(TechnologyFields.PRODUCT, product);
        newTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, getTechnologyType(order));

        newTechnology = newTechnology.getDataDefinition().save(newTechnology);

        return newTechnology;
    }

    private Entity copyTechnology(final Entity technologyPrototype, final Entity order) {
        Entity copyOfTechnology = technologyServiceO.getTechnologyDD().create();

        String number = numberGeneratorService.generateNumber(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        copyOfTechnology = technologyServiceO.getTechnologyDD().copy(technologyPrototype.getId()).get(0);

        copyOfTechnology.setField(TechnologyFields.NUMBER, number);
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, getTechnologyType(order));

        copyOfTechnology = copyOfTechnology.getDataDefinition().save(copyOfTechnology);

        return copyOfTechnology;
    }

    private void deleteTechnology(final Entity technology) {
        technology.getDataDefinition().delete(technology.getId());
    }

    private String getTechnologyType(final Entity order) {
        String orderType = order.getStringField(OrderFields.ORDER_TYPE);

        if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            return TechnologyType.WITH_OWN_TECHNOLOGY.getStringValue();
        } else {
            return TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue();
        }
    }

}
