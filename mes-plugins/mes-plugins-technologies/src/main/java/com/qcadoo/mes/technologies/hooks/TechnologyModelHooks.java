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
package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.MASTER;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechnologyModelHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private TechnologyStateChangeDescriber describer;

    public void setInitialState(final DataDefinition dataDefinition, final Entity technology) {
        stateChangeEntityBuilder.buildInitial(describer, technology, TechnologyState.DRAFT);
    }

    public void setNewMasterTechnology(final DataDefinition dataDefinition, final Entity technology) {
        if (!technology.getBooleanField(MASTER)) {
            return;
        }
        SearchCriteriaBuilder searchCriteries = dataDefinition.find();
        searchCriteries.add(SearchRestrictions.eq(MASTER, true));
        searchCriteries.add(SearchRestrictions.belongsTo(PRODUCT, technology.getBelongsToField(PRODUCT)));

        if (technology.getId() != null) {
            searchCriteries.add(SearchRestrictions.idNe(technology.getId()));
        }

        Entity defaultTechnology = searchCriteries.uniqueResult();
        if (defaultTechnology == null) {
            return;
        }
        defaultTechnology.setField(MASTER, false);
        dataDefinition.save(defaultTechnology);
    }

}
