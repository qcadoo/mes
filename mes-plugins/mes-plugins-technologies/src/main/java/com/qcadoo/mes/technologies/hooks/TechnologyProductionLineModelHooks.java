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


import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechnologyProductionLineModelHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNewMasterProductionLine(dataDefinition, entity);
    }

    private void setNewMasterProductionLine(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder searchCriteria = dataDefinition.find();
        searchCriteria.add(SearchRestrictions.eq(TechnologyProductionLineFields.MASTER, true));
        searchCriteria.add(SearchRestrictions.belongsTo(TechnologyProductionLineFields.TECHNOLOGY, entity.getBelongsToField(TechnologyProductionLineFields.TECHNOLOGY)));

        Entity masterTechnologyProductionLine = searchCriteria.uniqueResult();
        if (masterTechnologyProductionLine != null && masterTechnologyProductionLine.getId().equals(entity.getId()) ||
                masterTechnologyProductionLine == null || !entity.getBooleanField(TechnologyProductionLineFields.MASTER)) {
            return;
        }

        masterTechnologyProductionLine.setField(TechnologyProductionLineFields.MASTER, false);
        dataDefinition.save(masterTechnologyProductionLine);
    }

}
