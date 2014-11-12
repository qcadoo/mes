/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.MASTER;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;

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

    public void onCreate(final DataDefinition technologyDD, final Entity technology) {
        setInitialState(technology);
    }

    public void onCopy(final DataDefinition technologyDD, final Entity technology) {
        technology.setField(MASTER, false);
        technology.setField(TechnologyFields.EXTERNAL_SYNCHRONIZED, true);
        setInitialState(technology);
    }

    public void onSave(final DataDefinition technologyDD, final Entity technology) {
        setNewMasterTechnology(technologyDD, technology);
    }

    public void onUpdate(final DataDefinition technologyDD, final Entity technology) {
        performTreeNumbering(technologyDD, technology);
    }

    private void setInitialState(final Entity technology) {
        stateChangeEntityBuilder.buildInitial(describer, technology, TechnologyState.DRAFT);
    }

    private void setNewMasterTechnology(final DataDefinition technologyDD, final Entity technology) {
        if (technology.getStringField(TechnologyFields.STATE)
                .equals(TechnologyState.OUTDATED.getStringValue()) && technology.getBooleanField(MASTER)) {
            technology.setField(MASTER, false);
            return;
        }
        if (!technology.getStringField(TechnologyFields.STATE)
                .equals(TechnologyState.ACCEPTED.getStringValue())
                || technology.getStringField(TechnologyFields.TECHNOLOGY_TYPE) != null) {
            return;
        }
        SearchCriteriaBuilder searchCriteries = technologyDD.find();
        searchCriteries.add(SearchRestrictions.eq(MASTER, true));
        searchCriteries.add(SearchRestrictions.belongsTo(PRODUCT, technology.getBelongsToField(PRODUCT)));

        Entity defaultTechnology = searchCriteries.uniqueResult();
        if (defaultTechnology != null && defaultTechnology.getId().equals(technology.getId())) {
            return;
        }

        if (defaultTechnology == null && technology.getStringField(TechnologyFields.STATE)
                .equals(TechnologyState.ACCEPTED.getStringValue())) {
            technology.setField(MASTER, true);
            return;
        }

        if (defaultTechnology == null || !technology.getBooleanField(MASTER)) {
            return;
        }

        defaultTechnology.setField(MASTER, false);
        technologyDD.save(defaultTechnology);
    }

    public final void performTreeNumbering(final DataDefinition technologyDD, final Entity technology) {
        if (!technologyService.checkIfTechnologyStateIsOtherThanCheckedAndAccepted(technology)) {
            return;
        }
        DataDefinition technologyOperationDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        treeNumberingService.generateNumbersAndUpdateTree(technologyOperationDD, TechnologiesConstants.MODEL_TECHNOLOGY,
                technology.getId());
    }

}
