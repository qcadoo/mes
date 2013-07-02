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
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CopyOfTechnDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Transactional
    public void clearTechnology(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long technologyId = (Long) componentState.getFieldValue();

        if (technologyId != null) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();
            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            order.setField(OrderFields.TECHNOLOGY, null);
            order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);
            order.getDataDefinition().save(order);

            technologyDD.delete(technology.getId());

            Entity newCopyOfTechnology = technologyDD.create();

            newCopyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                    TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
            newCopyOfTechnology.setField(
                    TechnologyFields.NAME,
                    technologyServiceO.makeTechnologyName(newCopyOfTechnology.getStringField(TechnologyFields.NUMBER),
                            order.getBelongsToField(OrderFields.PRODUCT)));
            newCopyOfTechnology.setField(TechnologyFields.PRODUCT, order.getBelongsToField(OrderFields.PRODUCT));
            newCopyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, getTechnologyType(order));
            newCopyOfTechnology = newCopyOfTechnology.getDataDefinition().save(newCopyOfTechnology);
            order.setField(OrderFields.TECHNOLOGY, newCopyOfTechnology);

            order.getDataDefinition().save(order);
            technologyServiceO.setQuantityOfWorkstationTypes(order, newCopyOfTechnology);
            componentState.setFieldValue(newCopyOfTechnology.getId());
            final FormComponent form = (FormComponent) componentState;
            form.setEntity(newCopyOfTechnology);
        }
    }

    public void clearAndLoadPatternTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Long technologyId = (Long) componentState.getFieldValue();

        if (technologyId != null) {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();
            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            order.setField(OrderFields.TECHNOLOGY, null);
            order.getDataDefinition().save(order);
            technologyDD.delete(technology.getId());

            Entity newCopyOfTechnology = technologyDD.create();
            newCopyOfTechnology = technologyDD.copy(order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE).getId()).get(0);

            newCopyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                    TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
            newCopyOfTechnology.setField("technologyPrototype", order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE));
            newCopyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, getTechnologyType(order));
            newCopyOfTechnology = newCopyOfTechnology.getDataDefinition().save(newCopyOfTechnology);
            order.setField(OrderFields.TECHNOLOGY, newCopyOfTechnology);
            order.getDataDefinition().save(order);
            technologyServiceO.setQuantityOfWorkstationTypes(order, newCopyOfTechnology);
            componentState.setFieldValue(newCopyOfTechnology.getId());
            final FormComponent form = (FormComponent) componentState;
            form.setEntity(newCopyOfTechnology);

        }
    }

    public void changePatternTechnology(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long technologyId = (Long) componentState.getFieldValue();

        if (technologyId != null) {

            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);

            Entity technology = technologyDD.get(technologyId);
            LookupComponent patternTechnologyLookup = (LookupComponent) view.getComponentByReference("technologyPrototype");

            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();
            Entity patternTechnology = patternTechnologyLookup.getEntity();
            Entity newCopyOfTechnology = technologyDD.create();

            if (patternTechnology != null
                    && !patternTechnology.getId().equals(order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE).getId())) {
                order.setField(OrderFields.TECHNOLOGY, null);

                order.getDataDefinition().save(order);
                technologyDD.delete(technology.getId());
                newCopyOfTechnology = technologyDD.copy(patternTechnology.getId()).get(0);

                newCopyOfTechnology.setField(TechnologyFields.NUMBER, numberGeneratorService.generateNumber(
                        TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY));
                newCopyOfTechnology.setField("technologyPrototype", patternTechnology);
                newCopyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, getTechnologyType(order));
                newCopyOfTechnology = newCopyOfTechnology.getDataDefinition().save(newCopyOfTechnology);
                order.setField(OrderFields.TECHNOLOGY, newCopyOfTechnology);
                order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, patternTechnology);
                DataDefinition orderDD = dataDefinitionService
                        .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
                orderDD.save(order);
                // order.getDataDefinition().save(order);
                componentState.setFieldValue(newCopyOfTechnology.getId());
                final FormComponent form = (FormComponent) componentState;
                form.setEntity(newCopyOfTechnology);
            }

            if (newCopyOfTechnology.getId() == null) {
                technologyServiceO.setQuantityOfWorkstationTypes(order, technology);
            } else {
                technologyServiceO.setQuantityOfWorkstationTypes(order, newCopyOfTechnology);
            }
        }
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
