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

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class TechnologyServiceO {

    private static final String L_RANGE = "range";

    private static final String L_ONE_DIVISION = "01oneDivision";

    private static final String L_MANY_DIVISIONS = "02manyDivisions";

    private static final String L_DIVISION = "division";

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

    private void removeTechnologyFromOrder(final Entity order) {
        order.setField(OrderFields.TECHNOLOGY, null);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }

        return order.getDataDefinition().get(order.getId());
    }

    public DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    public Long getDefaultTechnology(final Long productId) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
        Entity dt = getDefaultTechnology(product);
        if(Objects.nonNull(dt)) {
            return dt.getId();
        }
        return null;
    }

    public Entity getDefaultTechnology(final Entity product) {
        SearchResult searchResult = getTechnologyDD().find()
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                .list();

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
        SearchResult searchResult = getTechnologyDD().find()
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                .list();
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


    public Optional<Entity> extractDivision(Entity technology, Entity technologyOperationComponent) {
        String range = technology.getStringField(L_RANGE);

        if (L_ONE_DIVISION.equals(range)) {
            return Optional.ofNullable(technology.getBelongsToField(L_DIVISION));
        } else if (L_MANY_DIVISIONS.equals(range) && Objects.nonNull(technologyOperationComponent)) {
            return Optional.ofNullable(technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
        }

        return Optional.empty();
    }

}
