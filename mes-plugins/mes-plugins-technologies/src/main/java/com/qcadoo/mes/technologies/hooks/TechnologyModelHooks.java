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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class TechnologyModelHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private TechnologyStateChangeDescriber describer;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private ParameterService parameterService;

    public void onCreate(final DataDefinition technologyDD, final Entity technology) {
        setInitialState(technology);
        fillRangeAndDivision(technology);
    }

    private void fillRangeAndDivision(final Entity technology) {
        String range = technology.getStringField(TechnologyFields.RANGE);
        Entity division = technology.getBelongsToField(TechnologyFields.DIVISION);

        if (StringUtils.isEmpty(range)) {
            range = parameterService.getParameter().getStringField(ParameterFieldsT.RANGE);

            if (StringUtils.isEmpty(range)) {
                range = Range.MANY_DIVISIONS.getStringValue();
            }
        }
        if (Objects.isNull(division)) {
            division = parameterService.getParameter().getBelongsToField(ParameterFieldsT.DIVISION);
        }

        technology.setField(TechnologyFields.RANGE, range);
        technology.setField(TechnologyFields.DIVISION, division);
    }

    public void onCopy(final DataDefinition technologyDD, final Entity technology) {
        technology.setField(TechnologyFields.MASTER, false);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);

        setInitialState(technology);
    }

    public void onSave(final DataDefinition technologyDD, final Entity technology) {
        if (!technology.getBooleanField(TechnologyFields.TEMPLATE)) {
            technology.setField(TechnologyFields.TEMPLATE, false);
        }

        if (Objects.isNull(technology.getField(TechnologyFields.IS_TEMPLATE_ACCEPTED))) {
            technology.setField(TechnologyFields.IS_TEMPLATE_ACCEPTED, false);
        }

        setNewMasterTechnology(technologyDD, technology);
        qualityCardChange(technologyDD, technology);
        fillDivision(technologyDD, technology);
    }

    private void fillDivision(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.nonNull(technology.getId())) {
            if (technology.getField(TechnologyFields.RANGE).equals(Range.ONE_DIVISION.getStringValue())) {
                List<Entity> tocs = getTechnologyOperationComponents(technology);

                for (Entity toc : tocs) {
                    toc.setField(TechnologyFields.DIVISION, technology.getBelongsToField(TechnologyFields.DIVISION));
                    toc.getDataDefinition().save(toc);
                }
            } else {
                technology.setField(TechnologyFields.DIVISION, null);
            }
            Entity technologyDB = technologyDD.get(technology.getId());
            if (isDivisionChanged(technology, technologyDB)) {
                Long[] productionLinesIds = technology.getHasManyField(TechnologyFields.PRODUCTION_LINES).stream().map(Entity::getId).toArray(Long[]::new);
                if (productionLinesIds.length > 0) {
                    getTechnologyProductionLineDD().delete(productionLinesIds);
                }
                List<Entity> tocs = getTechnologyOperationComponents(technology);

                clearWorkstations(tocs);
            }
        }
    }

    private boolean isDivisionChanged(Entity technology, Entity technologyDB) {
        return technology.getBelongsToField(TechnologyFields.DIVISION) != null
                && technologyDB.getBelongsToField(TechnologyFields.DIVISION) == null
                || technology.getField(TechnologyFields.RANGE).equals(Range.ONE_DIVISION.getStringValue())
                && technology.getBelongsToField(TechnologyFields.DIVISION) == null
                && technologyDB.getBelongsToField(TechnologyFields.DIVISION) != null
                || technology.getBelongsToField(TechnologyFields.DIVISION) != null
                && !technology.getBelongsToField(TechnologyFields.DIVISION).equals(technologyDB.getBelongsToField(TechnologyFields.DIVISION));
    }

    private void clearWorkstations(List<Entity> tocs) {
        for (Entity toc : tocs) {
            toc.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
            toc.getDataDefinition().save(toc);
        }
    }

    private List<Entity> getTechnologyOperationComponents(final Entity technology) {
        return getTechnologyOperationComponentDD().find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();
    }

    public void onUpdate(final DataDefinition technologyDD, final Entity technology) {
        performTreeNumbering(technologyDD, technology);
    }

    private void setInitialState(final Entity technology) {
        stateChangeEntityBuilder.buildInitial(describer, technology, TechnologyState.DRAFT);
    }

    private void setNewMasterTechnology(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.OUTDATED.getStringValue())
                && technology.getBooleanField(TechnologyFields.MASTER)) {
            technology.setField(TechnologyFields.MASTER, false);

            return;
        }

        if (!technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.ACCEPTED.getStringValue())) {
            return;
        }

        SearchCriteriaBuilder searchCriteriaBuilder = technologyDD.find();

        searchCriteriaBuilder.add(SearchRestrictions.eq(TechnologyFields.MASTER, true));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, technology.getBelongsToField(TechnologyFields.PRODUCT)));

        Entity defaultTechnology = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.nonNull(defaultTechnology) && defaultTechnology.getId().equals(technology.getId())) {
            return;
        }

        if (Objects.isNull(defaultTechnology)
                && technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.ACCEPTED.getStringValue())) {
            technology.setField(TechnologyFields.MASTER, true);

            return;
        }

        if (Objects.isNull(defaultTechnology) || !technology.getBooleanField(TechnologyFields.MASTER)) {
            return;
        }

        defaultTechnology.setField(TechnologyFields.MASTER, false);

        technologyDD.save(defaultTechnology);
    }

    public final void performTreeNumbering(final DataDefinition technologyDD, final Entity technology) {
        if (!technologyService.checkIfTechnologyStateIsOtherThanCheckedAndAccepted(technology)) {
            return;
        }

        treeNumberingService.generateNumbersAndUpdateTree(getTechnologyOperationComponentDD(), TechnologiesConstants.MODEL_TECHNOLOGY,
                technology.getId());
    }

    public void qualityCardChange(final DataDefinition technologyDD, final Entity technology) {
        if (Objects.nonNull(technology.getId())) {
            Entity qualityCard = technology.getBelongsToField(TechnologyFields.QUALITY_CARD);

            Entity technologyFromDB = technologyDD.get(technology.getId());
            Entity qualityCardFromDB = technologyFromDB.getBelongsToField(TechnologyFields.QUALITY_CARD);

            if (Objects.nonNull(qualityCardFromDB) && (Objects.isNull(qualityCard) || !qualityCardFromDB.getId().equals(qualityCard.getId()))) {
                for (Entity operationComponent : technologyFromDB.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
                    operationComponent.setField("qualityControlAttributesTOC", Collections.emptyList());

                    operationComponent.getDataDefinition().save(operationComponent);
                }
            }
        }
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getTechnologyProductionLineDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_PRODUCTION_LINE);
    }

}
