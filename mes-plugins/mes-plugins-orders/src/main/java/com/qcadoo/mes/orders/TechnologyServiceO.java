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
package com.qcadoo.mes.orders;

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyType;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TechnologyServiceO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    @Transactional
    public void createOrUpdateTechnology(final DataDefinition orderDD, final Entity order) {
        OrderType orderType = OrderType.of(order);
        Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (orderType == OrderType.WITH_PATTERN_TECHNOLOGY) {
            if (technologyPrototype == null) {
                removeTechnologyFromOrder(order);
            } else {
                createOrUpdateTechnologyForWithPatternTechnology(order, technologyPrototype);
            }
        } else {
            throw new IllegalStateException("Without pkt orderType must be set to WITH_PATTERN_TECHNOLOGY");
        }
    }

    @Transactional
    public Entity createTechnologyIfPktDisabled(final DataDefinition orderDD, Entity order) {
        OrderType orderType = OrderType.of(order);
        Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (!isTechnologyCopied(order)) {
            if (orderType == OrderType.WITH_PATTERN_TECHNOLOGY) {
                order.setField(OrderFields.TECHNOLOGY, copyTechnology(order, technologyPrototype));
                order = order.getDataDefinition().save(order);
            } else {
                throw new IllegalStateException("Without pkt orderType must be set to WITH_PATTERN_TECHNOLOGY");
            }
        }
        barcodeOperationComponentService.removeBarcode(order);
        return order;
    }

    private void removeTechnologyFromOrder(final Entity order) {
        order.setField(OrderFields.TECHNOLOGY, null);
    }

    private void createOrUpdateTechnologyForWithPatternTechnology(final Entity order, final Entity technologyPrototype) {
        if (isTechnologyCopied(order)) {
            if (isOrderTypeChangedToWithPatternTechnology(order)) {
                order.setField(OrderFields.TECHNOLOGY,
                        copyTechnology(order, technologyPrototype));
                barcodeOperationComponentService.removeBarcode(order);

            } else if (technologyWasChanged(order)) {
                order.setField(OrderFields.TECHNOLOGY, technologyPrototype);
                barcodeOperationComponentService.removeBarcode(order);
            }
        } else {
            order.setField(OrderFields.TECHNOLOGY, technologyPrototype);
        }
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }

        return order.getDataDefinition().get(order.getId());
    }

    private boolean isTechnologyCopied(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (technology == null) {
            return false;
        }

        return !Objects.equals(technology.getId(), technologyPrototype.getId());
    }

    private boolean isOrderTypeChangedToWithPatternTechnology(final Entity order) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        String orderType = existingOrder.getStringField(OrderFields.ORDER_TYPE);

        return !OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType);
    }

    private boolean technologyWasChanged(final Entity order) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity existingOrderTechnology = existingOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (existingOrderTechnology == null) {
            return true;
        }

        if (!existingOrderTechnology.equals(technology)) {
            return order.getBelongsToField(OrderFields.TECHNOLOGY) == null
                    || existingOrder.getBelongsToField(OrderFields.TECHNOLOGY) != null;
        } else {
            return false;
        }
    }

    private Entity copyTechnology(final Entity order, final Entity technologyPrototype) {
        Entity copyOfTechnology = getTechnologyDD().create();

        String number = generateNumberForTechnologyInOrder(order, technologyPrototype);

        copyOfTechnology = copyOfTechnology.getDataDefinition().copy(technologyPrototype.getId()).get(0);

        copyOfTechnology.setField(TechnologyFields.NUMBER, number);
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        copyOfTechnology.setField(TechnologyFields.TECHNOLOGY_TYPE, TechnologyType.WITH_PATTERN_TECHNOLOGY.getStringValue());

        copyOfTechnology = copyOfTechnology.getDataDefinition().save(copyOfTechnology);

        return copyOfTechnology;
    }

    public DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    public String makeTechnologyName(final String technologyNumber, final Entity product) {
        return technologyNumber + " - " + product.getStringField(ProductFields.NUMBER);
    }

    public Entity getDefaultTechnology(final Entity product) {
        SearchResult searchResult = getTechnologyDD().find().setMaxResults(1)
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).add(SearchRestrictions.eq("active", true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            Entity parent = product.getBelongsToField(ProductFields.PARENT);
            if (parent != null) {
                return getParentDefaultTechnology(parent);
            } else {
                return null;
            }
        }
    }

    private Entity getParentDefaultTechnology(final Entity product) {
        SearchResult searchResult = getTechnologyDD().find().setMaxResults(1)
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).add(SearchRestrictions.eq("active", true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).list();
        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    public String generateNumberForTechnologyInOrder(final Entity order, final Entity technology) {
        StringBuilder number = new StringBuilder();
        if (technology == null) {
            number.append(numberGeneratorService.generateNumber(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY));
        } else {
            number.append(technology.getStringField(TechnologyFields.NUMBER));
        }
        number.append(" - ");
        number.append(order.getStringField(OrderFields.NUMBER));
        number.append(" - ");
        return numberGeneratorService.generateNumberWithPrefix(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY, 3, number.toString());
    }

    public void changeTechnologyStateToChecked(Entity technology) {
        technology.setField(TechnologyFields.STATE, TechnologyStateStringValues.CHECKED);
        technology = technology.getDataDefinition().fastSave(technology);

        DataDefinition technologyStateChangeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_STATE_CHANGE);
        Entity technologyStateChange = technologyStateChangeDD.create();
        technologyStateChange.setField(TechnologyStateChangeFields.SOURCE_STATE, TechnologyStateStringValues.DRAFT);
        technologyStateChange.setField(TechnologyStateChangeFields.TARGET_STATE, TechnologyStateStringValues.CHECKED);
        technologyStateChange.setField(TechnologyStateChangeFields.TECHNOLOGY, technology);
        technologyStateChange.setField(TechnologyStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue());
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getDateTimeFieldName(), new Date());
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getShiftFieldName(),
                shiftsService.getShiftFromDateWithTime(new Date()));
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getWorkerFieldName(),
                securityService.getCurrentUserName());

        technologyStateChangeDD.fastSave(technologyStateChange);
    }

    public void changeTechnologyStateToAccepted(Entity technology) {
        technology.setField(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED);
        technology = technology.getDataDefinition().fastSave(technology);

        DataDefinition technologyStateChangeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_STATE_CHANGE);
        Entity technologyStateChange = technologyStateChangeDD.create();
        technologyStateChange.setField(TechnologyStateChangeFields.SOURCE_STATE, TechnologyStateStringValues.CHECKED);
        technologyStateChange.setField(TechnologyStateChangeFields.TARGET_STATE, TechnologyStateStringValues.ACCEPTED);
        technologyStateChange.setField(TechnologyStateChangeFields.TECHNOLOGY, technology);
        technologyStateChange.setField(TechnologyStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue());
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getDateTimeFieldName(), new Date());
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getShiftFieldName(),
                shiftsService.getShiftFromDateWithTime(new Date()));
        technologyStateChange.setField(technologyStateChangeAspect.getChangeEntityDescriber().getWorkerFieldName(),
                securityService.getCurrentUserName());
        technologyStateChangeDD.fastSave(technologyStateChange);
    }
}
